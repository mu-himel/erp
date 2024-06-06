package com.agi.aesl.erpscm.erpn_integration.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.network.NetworkService;

@Service
public class IntegrationWriterServiceImpl implements IntegrationWriterService{
    
    @Autowired
    private NetworkService networkService;

    @Value("${app.hr.create.warehouse}")
    private String warehouseCreateEndpoint;

    @Value("${app.hr.update.warehouse}")
    private String warehouseUpdateEndpoint;

    @Value("${app.hr.delete.warehouse}")
    private String warehouseDeleteEndpoint;

    @Override
    public void createWarehouse(Jwt token, Warehouse warehouse) {

        HttpHeaders headers = networkService.getHttpHeaders(token);
        Map<String,Object> data = new HashMap<>();
        data.put("warehouseName",warehouse.getName());
        data.put("warehouseLocation",warehouse.getLocation());
        HttpEntity<?> payload = new HttpEntity<>(data,headers);
        ResponseEntity<Void> response = networkService.post(warehouseCreateEndpoint, payload, Void.class);
        System.out.println(response.getStatusCode());
        
    }

    @Override
    public void deleteWarehouse(Jwt token, String warehouseName) {
        HttpHeaders headers = networkService.getHttpHeaders(token);
        Map<String,Object> data = new HashMap<>();
        data.put("warehouseName",warehouseName);
        HttpEntity<?> payload = new HttpEntity<>(data,headers);
        networkService.delete(warehouseDeleteEndpoint, payload, Void.class);
        
    }

    @Override
    public void updateWarehouse(Jwt token, String oldName, Warehouse warehouse) {
        HttpHeaders headers = networkService.getHttpHeaders(token);
        Map<String,Object> data = new HashMap<>();
        data.put("oldName",oldName);
        data.put("warehouseName",warehouse.getName());
        data.put("warehouseLocation",warehouse.getLocation());
        HttpEntity<?> payload = new HttpEntity<>(data,headers);
        networkService.put(warehouseUpdateEndpoint, payload, Void.class);
        
    }



    
}
