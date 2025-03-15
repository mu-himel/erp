package com.agi.aesl.erpscm.inventory.user_request.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryApproveDto;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryRejectDto;
import com.agi.aesl.erpscm.inventory.user_request.service.InventoryCategoryRequestService;
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
public class InventoryRequestCategoryController extends BaseController {


    private final InventoryRequestService inventoryRequestService;


    private final InventoryCategoryRequestService categoryRequestService;

    @GetMapping("/categories/my-requests")
    public ResponseEntity<Object> getMyCategories(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size")Optional<Integer> size
            ){
        return new ResponseEntity<>(
                categoryRequestService.getMyCategories(token,page,size),
                HttpStatus.OK);
    }

    @PostMapping("/categories")
    public ResponseEntity<Void> createCategory(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody CategoryRequestDto categoryRequestDto){
        categoryRequestService.createCategory(token,uri,categoryRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<Object> getCategory(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                categoryRequestService.getDetail(id),
            HttpStatus.OK
        );
    }

    @PostMapping("/subcategories")
    public ResponseEntity<Void> createSubCategory(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody CategoryRequestDto categoryRequestDto){
        categoryRequestService.createCategory(token,uri, categoryRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/categories/list")
    public ResponseEntity<Object> getList(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("name") Optional<String> name,
            @RequestParam("code") Optional<String> code,
            @RequestParam("warehouseId") Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId
    ){
        return new ResponseEntity<>(
                categoryRequestService.getCategories(token,name,code,warehouseId,warehouseStoreId),
                HttpStatus.OK
        );
    }

    @GetMapping("/subcategories/list")
    public ResponseEntity<Object> getList(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("name") Optional<String> name,
            @RequestParam("code") Optional<String> code
    ){
        return new ResponseEntity<>(
                categoryRequestService.getSubCategories(token,categoryId,name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/subcategories/my-requests")
    public ResponseEntity<Object> getMySubCategories(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size")Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getMySubCategories(token,categoryId,page,size),
                HttpStatus.OK);
    }

    @GetMapping("/categories/pending-verifications")
    public ResponseEntity<Object> getPendingVerification(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getPendingVerifications(token,Optional.empty(),page,size,true),
                HttpStatus.OK);
    }

    @GetMapping("/subcategories/pending-verifications")
    public ResponseEntity<Object> getSubCatPendingVerification(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getPendingVerifications(token,categoryId,page,size,false),
                HttpStatus.OK);
    }

    @GetMapping("/categories/pending-approvals")
    public ResponseEntity<Object> getPendingApprovals(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getPendingApprovals(token,Optional.empty(),page,size,true),
                HttpStatus.OK);
    }

    @GetMapping("/subcategories/pending-approvals")
    public ResponseEntity<Object> getSubCatPendingApprovals(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getPendingApprovals(token,categoryId,page,size,false),
                HttpStatus.OK);
    }

    @GetMapping("/categories/closed")
    public ResponseEntity<Object> getClosed(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getClosed(token,Optional.empty(),page,size,true),
                HttpStatus.OK);
    }

    @GetMapping("/categories/pending-approvals-store")
    public ResponseEntity<Object> getPendingApprovalCategoriesByStore(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("name") Optional<String> name,
            @RequestParam("warehouseId") Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId

    ){
        return new ResponseEntity<>(
                categoryRequestService.getPendingApprovalCategoriesFromStore(token,Optional.empty(),
                        name,
                        warehouseId,warehouseStoreId
                        ,page,size,true),
                HttpStatus.OK);
    }

    @GetMapping("/subcategories/pending-approvals-store")
    public ResponseEntity<Object> getPendingApprovalSubCategoriesByStore(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("name") Optional<String> name,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("warehouseId") Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId
    ){
        return new ResponseEntity<>(
                categoryRequestService.getPendingApprovalSubCategoriesFromStore(token,categoryId,
                        name,warehouseId,warehouseStoreId,
                        page,size,false),
                HttpStatus.OK);
    }

    @GetMapping("/subcategories/closed")
    public ResponseEntity<Object> getClosedSubCategories(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getClosed(token,categoryId,page,size,false),
                HttpStatus.OK);
    }

    @PutMapping("/categories/review/{id}")
    public ResponseEntity<Void> reviewCat(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
    ){
       categoryRequestService.review(token,id,reviewDto);
       return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/subcategories/review/{id}")
    public ResponseEntity<Void> reviewSubCat(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
    ){
        categoryRequestService.review(token,id,reviewDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/approve-by-store")
    public ResponseEntity<Void> approveByStore(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody CategoryApproveDto approveDto){
        categoryRequestService.approveByStore(token,id,approveDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/reject-by-store")
    public ResponseEntity<Void> rejectByStore(@PathVariable("id") Long id,
                                           @RequestBody CategoryRejectDto rejectDto
                                           ){
        categoryRequestService.rejectByStore(id, rejectDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
