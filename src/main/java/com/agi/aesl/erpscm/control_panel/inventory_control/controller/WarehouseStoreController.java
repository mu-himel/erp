package com.agi.aesl.erpscm.control_panel.inventory_control.controller;

import com.agi.aesl.erpscm.common.BaseController;
// import com.agi.aesl.erpscm.control_panel.inventory_control.dto.CopyToStoreDto;
import com.agi.aesl.erpscm.control_panel.inventory_control.dto.StoreDto;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseStoreService;

// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/warehouses/stores")
public class WarehouseStoreController extends BaseController{

    @Autowired
    private WarehouseStoreService warehouseStoreService;

    @PostMapping
    // @ApiOperation(value = "Create Store")
    public ResponseEntity<?> createStore(@RequestBody @Valid  StoreDto storeDto){
        warehouseStoreService.createStore(storeDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "/{warehouseId}",params = "page")
    public ResponseEntity<?> getStoresByWarehouse(@PathVariable("warehouseId") Long warehouseId,
                                                  @RequestParam("page") Optional<Integer> page,
                                                  @RequestParam("size") Optional<Integer> size
                                                  ){
        return new ResponseEntity<>(
                warehouseStoreService.getStores(warehouseId,page,size),
                HttpStatus.OK
        );
    }

    @GetMapping(value = "/{warehouseId}",params = "single")
    public ResponseEntity<?> getStoresByWarehouse(@PathVariable("warehouseId") Long warehouseId
    ){
        return new ResponseEntity<>(
                warehouseStoreService.getStore(warehouseId),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllStoresByWarehouse(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("warehouseId") Optional<Long> warehouseId
    ){
        return new ResponseEntity<>(
                warehouseStoreService.getStoresByWarehouse(token,warehouseId),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWarehouseStore(@PathVariable("id") Long id, @RequestBody StoreDto storeDto){
        warehouseStoreService.updateStore(id,storeDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @DeleteMapping("/{id}")
    // @ApiOperation(value = "Delete Warehouse store by id")
    public ResponseEntity<?> deleteWarehouseStore(@PathVariable("id") Long id){
        warehouseStoreService.deleteWarehouseStore(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/store-sub-categories")
    public ResponseEntity<?> getStoreSubCategories(
            @RequestParam("warehouseId") Long warehouseId,
            @RequestParam("storeId") Long storeId
    ){
        return new ResponseEntity<>(warehouseStoreService.getStoreSubCategories(warehouseId,storeId),
                HttpStatus.OK);
    }

    // @PostMapping("/copy-to-store/{warehouseId}")
    // public ResponseEntity<?> copyToFinishGoods(
    //         @PathVariable("warehouseId") Long warehouseId,
    //         @RequestBody CopyToStoreDto copyToStoreDto
    //                                            ){
    //     warehouseStoreService.copyToStore(warehouseId, copyToStoreDto);
    //     return new ResponseEntity<>(HttpStatus.CREATED);
    // }
}
