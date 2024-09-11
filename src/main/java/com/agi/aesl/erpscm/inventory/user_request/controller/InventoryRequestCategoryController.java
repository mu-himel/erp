package com.agi.aesl.erpscm.inventory.user_request.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.inventory.user_request.service.InventoryRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/inventory-requests")
public class InventoryRequestCategoryController extends BaseController {

    @Autowired
    private InventoryRequestService inventoryRequestService;

    @GetMapping("/categories/my-requests")
    public ResponseEntity<?> getMyCategories(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size")Optional<Integer> size
            ){
        return new ResponseEntity<>(
                inventoryRequestService.getMyCategories(token,page,size),
                HttpStatus.OK);
    }

    @GetMapping("/subcategories/my-requests")
    public ResponseEntity<?> getMySubCategories(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size")Optional<Integer> size
    ){
        return new ResponseEntity<>(
                inventoryRequestService.getMySubCategories(token,page,size),
                HttpStatus.OK);
    }

    @GetMapping("/products/my-requests")
    public ResponseEntity<?> getMyProducts(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size")Optional<Integer> size
    ){
        return new ResponseEntity<>(
                inventoryRequestService.getMyProducts(token,page,size),
                HttpStatus.OK);
    }
}
