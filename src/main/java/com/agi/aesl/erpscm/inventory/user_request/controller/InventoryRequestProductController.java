package com.agi.aesl.erpscm.inventory.user_request.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.UserItemRequestDto;
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
            @RequestBody UserItemRequestDto itemRequestDto
            ){
        inventoryRequestService.createProduct(token,uri,itemRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/products/my-requests")
    public ResponseEntity<?> getMyProducts(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size")Optional<Integer> size
    ){
        return new ResponseEntity<>(
                inventoryRequestService.getMyProducts(token,categoryId,subCategoryId,page,size),
                HttpStatus.OK);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getDetail(
            @PathVariable("id") Long id
    ){
        return new ResponseEntity<>(
                inventoryRequestService.getDetail(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/products/pending-verifications")
    public ResponseEntity<?> getPendingVerifications(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                inventoryRequestService.getPendingVerifications(token, categoryId, subCategoryId,
                        page,size),
                HttpStatus.OK
        );
    }

    @GetMapping("/products/pending-approvals")
    public ResponseEntity<?> getPendingApprovals(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                inventoryRequestService.getPendingApprovals(token,categoryId,subCategoryId,page,size),
                HttpStatus.OK
        );
    }

    @GetMapping("/products/closed")
    public ResponseEntity<?> getClosed(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                inventoryRequestService.getClosed(token,categoryId,subCategoryId,page,size),
                HttpStatus.OK
        );
    }

    @PutMapping("/products/review/{id}")
    public ResponseEntity<?> reviewProduct(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
            ){
        inventoryRequestService.review(token, id, reviewDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
