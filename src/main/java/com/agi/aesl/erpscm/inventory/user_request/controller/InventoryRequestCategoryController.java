package com.agi.aesl.erpscm.inventory.user_request.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.user_request.service.InventoryCategoryRequestService;
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
public class InventoryRequestCategoryController extends BaseController {

    @Autowired
    private InventoryRequestService inventoryRequestService;

    @Autowired
    private InventoryCategoryRequestService categoryRequestService;

    @GetMapping("/categories/my-requests")
    public ResponseEntity<?> getMyCategories(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size")Optional<Integer> size
            ){
        return new ResponseEntity<>(
                categoryRequestService.getMyCategories(token,page,size),
                HttpStatus.OK);
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody CategoryRequestDto categoryRequestDto){
        categoryRequestService.createCategory(token,uri,categoryRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/sub-categories")
    public ResponseEntity<?> createSubCategory(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody CategoryRequestDto categoryRequestDto){
        categoryRequestService.createCategory(token,uri, categoryRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/subcategories/my-requests")
    public ResponseEntity<?> getMySubCategories(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size")Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getMySubCategories(token,page,size),
                HttpStatus.OK);
    }

    @GetMapping("/categories/pending-verifications")
    public ResponseEntity<?> getPendingVerification(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getPendingVerifications(token,page,size,true),
                HttpStatus.OK);
    }
}
