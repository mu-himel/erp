package com.agi.aesl.erpscm.purchase_order.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.purchase_order.dto.request.PurchaseRequestDto;
import com.agi.aesl.erpscm.purchase_order.service.PurchaseOrderService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/po")
@RequiredArgsConstructor
public class PurchaseController extends BaseController {


    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<Void> addPurchaseOrder(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody PurchaseRequestDto purchaseRequestDto
    ){
        purchaseOrderService.generatePurchaseOrder(token,uri,purchaseRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/pending")
    public ResponseEntity<Object> getPendingPurchaseOrders(
            @RequestParam("vendor") Optional<String> vendor,
            @RequestParam("csNo") Optional<String> csNo,
            @RequestParam("poNo") Optional<String> poNo,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("status") Optional<String> status,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                purchaseOrderService.getPendingPOs(vendor,csNo,
                        poNo,categoryId,subCategoryId,fromDateStr,toDateStr,status,
                        page,size),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<Object> getPendingVerificationPurchaseOrders(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("csNo") Optional<String> csNo,
            @RequestParam("poNo") Optional<String> poNo,
            @RequestParam("vendor") Optional<String> vendor,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("status") Optional<String> status,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
            ){
        return new ResponseEntity<>(
                purchaseOrderService.getPendingVerificationPOs(token,vendor,
                        csNo,poNo,categoryId,subCategoryId,fromDateStr,toDateStr,status,
                        page,size),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<Object> getPendingApprovalPurchaseOrders(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("csNo") Optional<String> csNo,
            @RequestParam("poNo") Optional<String> poNo,
            @RequestParam("vendor") Optional<String> vendor,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("status") Optional<String> status,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                purchaseOrderService.getPendingApprovalPOs(token,vendor,
                        csNo,poNo,categoryId,subCategoryId,
                        fromDateStr,toDateStr,status,page, size),
                HttpStatus.OK
        );
    }

    @GetMapping("/approved")
    public ResponseEntity<Object> getApprovedPurchaseOrders(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("vendor") Optional<String> vendor,
            @RequestParam("csNo") Optional<String> csNo,
            @RequestParam("poNo") Optional<String> poNo,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("status") Optional<String> status,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                purchaseOrderService.getApprovedPOs(token,
                        vendor,csNo,poNo,categoryId,subCategoryId,
                        fromDateStr,toDateStr, status,
                        page,size),
                HttpStatus.OK
        );
    }

    @GetMapping("/closed")
    public ResponseEntity<Object> getClosedPurchaseOrders(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("vendor") Optional<String> vendor,
            @RequestParam("csNo") Optional<String> csNo,
            @RequestParam("poNo") Optional<String> poNo,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("status") Optional<String> status,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                purchaseOrderService.getClosedPOs(token,
                        vendor,csNo,poNo,categoryId,subCategoryId,
                        fromDateStr,toDateStr,status,
                        page, size),
                HttpStatus.OK
        );
    }

    @GetMapping("/{csId}")
    public ResponseEntity<Object> getPoDetail(@PathVariable("csId") Long csId){
        return new ResponseEntity<>(
                purchaseOrderService.getPurchaseOrderDetail(csId),
                HttpStatus.OK
        );
    }

    @PutMapping("/submit-for-verification/{csId}")
    public ResponseEntity<Void> submitForVerification(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @PathVariable("csId") Long csId
    ){
        purchaseOrderService.setVerificationAndApproval(token, uri, csId);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<Void> reviewPo(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody NoteDto noteDto
    ){
        purchaseOrderService.reviewPo(token, id, noteDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> rejectPo(
            @AuthenticationPrincipal Jwt loggedInUser,
            @PathVariable("id") Long id,
            @RequestBody NoteDto noteDto
    ){
        purchaseOrderService.rejectPo(loggedInUser, id, noteDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
