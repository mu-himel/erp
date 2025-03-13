package com.agi.aesl.erpscm.control_panel.inventory_control.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController extends BaseController{


    private final WarehouseService warehouseService;


    @PostMapping
    public ResponseEntity<Void> createWarehouse(
        @AuthenticationPrincipal Jwt token,
        @RequestBody @Valid Warehouse warehouse){
        warehouseService.createWarehouse(token, warehouse);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Object> getWarehouses(
            @AuthenticationPrincipal Jwt token,
                @RequestParam("name") Optional<String> name,
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size

                ){
        return new ResponseEntity<>(
                warehouseService.getWarehouses(token,name,page,size),
                HttpStatus.OK
        );
    }

    @GetMapping("/list")
    public ResponseEntity<Object> getWarehouses(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("name") Optional<String> name){
        return new ResponseEntity<>(
                warehouseService.getWarehouses(token, name),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getWarehouse(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                warehouseService.getWarehouse(id),
                HttpStatus.OK
        );
    }

    @PutMapping
    public ResponseEntity<Void> updateWarehouse(
        @AuthenticationPrincipal Jwt token,
        @RequestBody @Valid Warehouse warehouse){
        warehouseService.updateWarehouse(token, warehouse);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(
        @AuthenticationPrincipal Jwt token,
        @PathVariable("id") Long id){
        warehouseService.deleteWarehouse(token, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
