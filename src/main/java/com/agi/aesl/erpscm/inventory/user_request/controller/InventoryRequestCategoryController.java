package com.agi.aesl.erpscm.inventory.user_request.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
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

    @GetMapping("/categories/{id}")
    public ResponseEntity<?> getCategory(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                categoryRequestService.getDetail(id),
            HttpStatus.OK
        );
    }

    @PostMapping("/subcategories")
    public ResponseEntity<?> createSubCategory(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody CategoryRequestDto categoryRequestDto){
        categoryRequestService.createCategory(token,uri, categoryRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/categories/list")
    public ResponseEntity<?> getList(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("name") Optional<String> name,
            @RequestParam("code") Optional<String> code
    ){
        return new ResponseEntity<>(
                categoryRequestService.getCategories(token,name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/subcategories/list")
    public ResponseEntity<?> getList(
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
    public ResponseEntity<?> getMySubCategories(
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
    public ResponseEntity<?> getPendingVerification(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getPendingVerifications(token,Optional.empty(),page,size,true),
                HttpStatus.OK);
    }

    @GetMapping("/subcategories/pending-verifications")
    public ResponseEntity<?> getSubCatPendingVerification(
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
    public ResponseEntity<?> getPendingApprovals(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getPendingApprovals(token,Optional.empty(),page,size,true),
                HttpStatus.OK);
    }

    @GetMapping("/subcategories/pending-approvals")
    public ResponseEntity<?> getSubCatPendingApprovals(
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
    public ResponseEntity<?> getClosed(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getClosed(token,Optional.empty(),page,size,true),
                HttpStatus.OK);
    }

    @GetMapping("/categories/pending-approvals-store")
    public ResponseEntity<?> getPendingApprovalCategoriesByStore(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getPendingApprovalCategoriesFromStore(token,Optional.empty(),page,size,true),
                HttpStatus.OK);
    }

    @GetMapping("/subcategories/pending-approvals-store")
    public ResponseEntity<?> getPendingApprovalSubCategoriesByStore(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getPendingApprovalSubCategoriesFromStore(token,categoryId,page,size,false),
                HttpStatus.OK);
    }

    @GetMapping("/subcategories/closed")
    public ResponseEntity<?> getClosedSubCategories(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryRequestService.getClosed(token,categoryId,page,size,true),
                HttpStatus.OK);
    }

    @PutMapping("/categories/review/{id}")
    public ResponseEntity<?> reviewCat(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
    ){
       categoryRequestService.review(token,id,reviewDto);
       return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/subcategories/review/{id}")
    public ResponseEntity<?> reviewSubCat(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
    ){
        categoryRequestService.review(token,id,reviewDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/approved-by-store")
    public ResponseEntity<?> approveByStore(@PathVariable("id") Long id){
        categoryRequestService.approveByStore(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/reject-by-store")
    public ResponseEntity<?> rejectByStore(@PathVariable("id") Long id){
        categoryRequestService.rejectByStore(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
