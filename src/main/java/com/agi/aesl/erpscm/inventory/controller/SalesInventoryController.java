package com.agi.aesl.erpscm.inventory.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.inventory.service.SalesInventoryService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/sales")
public class SalesInventoryController extends BaseController {

    @Autowired
    private SalesInventoryService salesInventoryService;

    @GetMapping("/warehouses")
    public ResponseEntity<?> getWarehouses(
                @RequestParam("name") Optional<String> name,
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size
                ){
        return new ResponseEntity<>(salesInventoryService.getWarehouses(name,page,size),HttpStatus.OK);
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(
                @RequestParam("warehouseId")  Optional<Long> warehouseId,
                @RequestParam("name") Optional<String> name,
                @RequestParam("code") Optional<String> code,
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size
    ){
        return  new ResponseEntity<>(
                salesInventoryService.getCategories(warehouseId,name,code,page,size),
                HttpStatus.OK);
    }

    @GetMapping("/sub-categories")
    public ResponseEntity<?> getCategories(
            @RequestParam("warehouseId") Optional<Long> warehouseId,
            @RequestParam("categoryId")  Optional<Long> categoryId,
            @RequestParam("name")  Optional<String> name,
            @RequestParam("code")  Optional<String> code,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
            ){
        return  new ResponseEntity<>(
                salesInventoryService.getSubCategories(warehouseId,categoryId,name,code,page,size),
                HttpStatus.OK);
    }

    @GetMapping("/sub-categories/by-code/{warehouseId}")
    public ResponseEntity<?> getCategories(@PathVariable  Long warehouseId,@RequestParam("categoryCode") String categoryCode){
        return  new ResponseEntity<>(
                salesInventoryService.getSubCategories(warehouseId,categoryCode),
                HttpStatus.OK);
    }

    @GetMapping("/products")
    public ResponseEntity<?> getProducts(@RequestParam("warehouseId")  Optional<Long> warehouseId,
                                         @RequestParam("categoryId") Optional<Long> categoryId,
                                         @RequestParam("subCategoryId") Optional<Long> subCategoryId,
                                         @RequestParam("page") Optional<Integer> page,
                                         @RequestParam("size") Optional<Integer> size
    ){
        return  new ResponseEntity<>(
                salesInventoryService.getProducts(warehouseId,categoryId,subCategoryId,page,size),
                HttpStatus.OK);
    }
}
