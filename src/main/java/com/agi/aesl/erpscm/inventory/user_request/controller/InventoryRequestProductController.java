package com.agi.aesl.erpscm.inventory.user_request.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.inventory.dto.request.UserItemRequestDto;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryApproveDto;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryRejectDto;
import com.agi.aesl.erpscm.inventory.user_request.service.InventoryRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/inventory-requests")
@RequiredArgsConstructor
public class InventoryRequestProductController extends BaseController {


    private final InventoryRequestService inventoryRequestService;

    @PostMapping("/products")
    public ResponseEntity<Void> createProduct(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody UserItemRequestDto itemRequestDto
            ){
        inventoryRequestService.createProduct(token,uri,itemRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/products/my-requests")
    public ResponseEntity<Object> getMyProducts(
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
    public ResponseEntity<Object> getDetail(
            @PathVariable("id") Long id
    ){
        return new ResponseEntity<>(
                inventoryRequestService.getDetail(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/products/pending-verifications")
    public ResponseEntity<Object> getPendingVerifications(
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
    public ResponseEntity<Object> getPendingApprovals(
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
    public ResponseEntity<Object> getClosed(
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

    @GetMapping("/products/pending-approvals-store")
    public ResponseEntity<Object> getPendingApprovalsByStore(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("warehouseId") Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                inventoryRequestService.getPendingApprovalItemsByStore(token,categoryId,subCategoryId,
                        warehouseId,warehouseStoreId,
                        page,size),
                HttpStatus.OK
        );
    }


    @PutMapping("/products/review/{id}")
    public ResponseEntity<Void> reviewProduct(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
            ){
        inventoryRequestService.review(token, id, reviewDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/products/approve-by-store/{id}")
    public ResponseEntity<Void> approveByStore(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody CategoryApproveDto itemApproveDto){
        inventoryRequestService.approveByStore(token,id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/products/reject-by-store/{id}")
    public ResponseEntity<Void> rejectByStore(@PathVariable("id") Long id,
                                           @RequestBody CategoryRejectDto rejectDto){
        inventoryRequestService.rejectByStore(id,rejectDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
