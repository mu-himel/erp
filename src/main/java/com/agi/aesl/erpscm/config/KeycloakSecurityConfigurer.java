package com.agi.aesl.erpscm.config;

import java.io.IOException;
import java.util.Set;

import org.keycloak.adapters.authorization.integration.jakarta.ServletPolicyEnforcerFilter;
import org.keycloak.adapters.authorization.spi.ConfigurationResolver;
import org.keycloak.adapters.authorization.spi.HttpRequest;
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

    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        DelegatingJwtGrantedAuthoritiesConverter djgac = new DelegatingJwtGrantedAuthoritiesConverter(
            new JwtGrantedAuthoritiesConverter(),
            new KeycloakJwtTokenConverter());

        httpSecurity.oauth2ResourceServer(server -> {
            
            server.jwt().jwtAuthenticationConverter(jwt ->{
                
                return new JwtAuthenticationToken(jwt, djgac.convert(jwt));
            } );
        });

        httpSecurity
                .csrf(csrf -> csrf.disable()).authorizeHttpRequests((authorize) -> {
            authorize.requestMatchers("/api/v1/**").authenticated();
        }).addFilterBefore(createServletPolicyFilter(), BearerTokenAuthenticationFilter.class)
        
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                        

        
        return httpSecurity.build();
    }

    private ServletPolicyEnforcerFilter createServletPolicyFilter(){
        PolicyEnforcerConfig config;

        try {
            config = JsonSerialization.readValue(getClass().getResourceAsStream("/policy-enforcer.json"), PolicyEnforcerConfig.class);
        } catch (IOException e) {
            
            throw new RuntimeException(e);
        }
        return new ServletPolicyEnforcerFilter(new ConfigurationResolver() {

            @Override
            public PolicyEnforcerConfig resolve(HttpRequest request) {
                if(request.getPrincipal() == null){
                    throw new RuntimeException("Sorry! Token not valid");
                }
                if(request.getPrincipal().getToken().getRealmAccess()!=null){
                    // HttpServletRequest req = (HttpServletRequest) request;
                    
                    Set<String> roles = request.getPrincipal().getToken().getRealmAccess().getRoles();
                    // System.out.println("ROLES"+roles);
                    // if(!roles.contains("ADMIN")){
                    //     throw new RuntimeException("Sorry! Need Admin Profile to access this");
                    // }
                };
                return config;
            }
            
        });
    
    }

    
    
}
