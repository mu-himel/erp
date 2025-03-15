package com.agi.aesl.erpscm.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.agi.aesl.erpscm.exception.AesException;
import org.keycloak.adapters.authorization.integration.jakarta.ServletPolicyEnforcerFilter;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig;
import org.keycloak.util.JsonSerialization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
@EnableWebSecurity
public class KeycloakSecurityConfigurer {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    JwtDecoder jwtDecoder(){
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        DelegatingJwtGrantedAuthoritiesConverter djgac = new DelegatingJwtGrantedAuthoritiesConverter(
            new JwtGrantedAuthoritiesConverter(),
            new KeycloakJwtTokenConverter());

        httpSecurity.oauth2ResourceServer(server ->
            
            server.jwt(jwtConfigurer -> jwtConfigurer
                    .jwtAuthenticationConverter(jwt->new JwtAuthenticationToken(jwt,djgac.convert(jwt))))
        );

        httpSecurity
                .csrf(csrf -> csrf.disable()).authorizeHttpRequests(authorize ->
            authorize.requestMatchers("/warehouses").permitAll().anyRequest().authenticated()
        ).addFilterBefore(createServletPolicyFilter(), BearerTokenAuthenticationFilter.class)
        
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                        

        
        return httpSecurity.build();
    }

    private void addExcludedPaths(PolicyEnforcerConfig config) {
        List<String> excludeUrls = List.of("/warehouses");
        List<PolicyEnforcerConfig.PathConfig> paths = new ArrayList<>();
        List<PolicyEnforcerConfig.MethodConfig> methods = new ArrayList<>();
        for (String url : excludeUrls) {
            PolicyEnforcerConfig.PathConfig excludedPath = new PolicyEnforcerConfig.PathConfig();
            excludedPath.setPath(url);
            PolicyEnforcerConfig.MethodConfig m = new PolicyEnforcerConfig.MethodConfig();
            PolicyEnforcerConfig.MethodConfig m1 = new PolicyEnforcerConfig.MethodConfig();
            m.setMethod("GET");
            m1.setMethod("POST");
            methods.add(m);
            methods.add(m1);
            excludedPath.setMethods(methods);
            excludedPath.setEnforcementMode(PolicyEnforcerConfig.EnforcementMode.DISABLED);
            paths.add(excludedPath);
        }
        config.setPaths(paths);
    }
    private ServletPolicyEnforcerFilter createServletPolicyFilter(){
        PolicyEnforcerConfig config;
        try {
            config = JsonSerialization.readValue(getClass().getResourceAsStream("/policy-enforcer.json"), PolicyEnforcerConfig.class);
            addExcludedPaths(config);
        } catch (IOException e) {
            
            throw new AesException(e.getMessage());
        }
        return new ServletPolicyEnforcerFilter(request-> {


                if(request.getPrincipal() == null){
                    throw new AesException("Sorry! Token not valid");
                }
                return config;

            
        });
    
    }

    
    
}
