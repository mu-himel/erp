package com.agi.aesl.erpscm.network;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NetworkService {
    
    @Autowired
    private RestTemplate restTemplate;

    public <T,P>ResponseEntity<T> post(String url, HttpEntity<P> payload, Class<T> t){
        return restTemplate.postForEntity(url, payload,  t);
    }

    public <T,P>ResponseEntity<T> put(String url, HttpEntity<P> payload, Class<T> t){
        return restTemplate.exchange(url,HttpMethod.PUT, payload,  t);
    }

    public <T> ResponseEntity<T> get(String url, Class<T> t){
        return restTemplate.getForEntity(url,t);
    }
    public <T,P> ResponseEntity<T> get(String url, HttpEntity<P> header, Class<T> t){
        return restTemplate.exchange(url,HttpMethod.GET,header,t);
    }
}
