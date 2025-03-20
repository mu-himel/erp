package com.agi.aesl.erpscm.network;

import com.agi.aesl.erpscm.organization.entity.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class NetworkService {
    
    private final RestTemplate restTemplate;

    private static final String BEARER="Bearer ";

    public HttpHeaders setHttpHeaders(Jwt token){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization",BEARER+token.getTokenValue());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public HttpHeaders setHttpHeaders(Organization organization){
        HttpHeaders headers = new HttpHeaders();
        headers.set("OrgId",organization.getCpsVendorRegistrationId().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }



    public HttpHeaders setHttpHeaders(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization",BEARER+ token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public HttpHeaders setHttpHeadersForHr(Jwt token){
        HttpHeaders headers = new HttpHeaders();
        headers.set("KCAuthorization",BEARER+token.getTokenValue());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public <T,P>ResponseEntity<T> post(String url, HttpEntity<P> payload, Class<T> t){
        return restTemplate.postForEntity(url, payload,  t);
    }

    public <T,P>ResponseEntity<T> put(String url, HttpEntity<P> payload, Class<T> t){
        return restTemplate.exchange(url,HttpMethod.PUT, payload,  t);
    }

    public <T,P>ResponseEntity<T> delete(String url, HttpEntity<P> payload, Class<T> t){
        return restTemplate.exchange(url,HttpMethod.DELETE, payload,  t);
    }

    public <T> ResponseEntity<T> get(String url, Class<T> t){
        return restTemplate.getForEntity(url,t);
    }
    public <T,P> ResponseEntity<T> get(String url, HttpEntity<P> header, Class<T> t){
        return restTemplate.exchange(url,HttpMethod.GET,header,t);
    }
}
