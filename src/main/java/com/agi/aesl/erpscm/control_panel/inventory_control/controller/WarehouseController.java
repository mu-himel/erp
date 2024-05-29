package com.agi.aesl.erpscm.control_panel.inventory_control.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;

import jakarta.validation.Valid;

// import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController extends BaseController{

    @Autowired
    private WarehouseService warehouseService;


    @PostMapping
    // @ApiOperation(value = "Create Warehouse with Store")
    public ResponseEntity<?> createWarehouse(@RequestBody @Valid Warehouse warehouse){
        warehouseService.createWarehouse(warehouse);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    // @ApiOperation(value = "Get Warehouse with pagination")
    public ResponseEntity<?> getWarehouses(
                @RequestParam("name") Optional<String> name,
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size

                ){
        return new ResponseEntity<>(
                warehouseService.getWarehouses(name,page,size),
                HttpStatus.OK
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getWarehouses(@RequestParam("name") Optional<String> name){
        return new ResponseEntity<>(
                warehouseService.getWarehouses(name),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWarhouse(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                warehouseService.getWarehouse(id),
                HttpStatus.OK
        );
    }

    @PutMapping
    // @ApiOperation(value = "Update Warehouse info")
    public ResponseEntity<?> updateWarehouse(@RequestBody @Valid Warehouse warehouse){
        warehouseService.updateWarehouse(warehouse);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    // @ApiOperation(value = "Delete Warehouse by id")
    public ResponseEntity<?> deleteWarehouse(@PathVariable("id") Long id){
        warehouseService.deleteWarehouse(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
