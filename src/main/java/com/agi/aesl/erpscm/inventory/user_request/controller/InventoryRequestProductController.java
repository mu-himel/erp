package com.agi.aesl.erpscm.inventory.user_request.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import com.agi.aesl.erpscm.inventory.user_request.service.InventoryRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/inventory-requests")
public class InventoryRequestProductController extends BaseController {

    @Autowired
    private InventoryRequestService inventoryRequestService;

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody ItemRequestDto itemRequestDto
            ){
        inventoryRequestService.createProduct(token,uri,itemRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
