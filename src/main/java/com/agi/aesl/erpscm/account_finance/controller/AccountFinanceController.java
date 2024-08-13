package com.agi.aesl.erpscm.account_finance.controller;

import com.agi.aesl.erpscm.account_finance.dto.request.LedgerAccountRequestDto;
import com.agi.aesl.erpscm.account_finance.service.AccountService;
import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountFinanceController extends BaseController {

    @Autowired
    private AccountService accountService;

    record LedgerRequest(Long id, String assetNo, String store, String category, String subCategory, String product,
                         String group, String status){}


    @PutMapping("/{id}")
    public ResponseEntity<?> updateLedgerAccount(
            @RequestHeader("uri") String uri,
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id, @RequestBody LedgerAccountRequestDto ledgerAccountRequestDto){
        accountService.updateAccount(token, uri,  id, ledgerAccountRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/review/{id}")
    public ResponseEntity<?> review(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
    ){
        accountService.review(token,id,reviewDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }



    @GetMapping("/ledgers")
    public ResponseEntity<?> getLedgerRequests(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("warehouseId") Optional<Long> warehouseId
            ){
        return new ResponseEntity<>(accountService.getAllPendingAccounts(token,page,size,warehouseId), HttpStatus.OK);
    }

    @GetMapping("/ledgers/closed")
    public ResponseEntity<?> getClosedLedgerRequests(
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(accountService.getClosedAccounts(page,size),HttpStatus.OK);
    }

    @GetMapping("/ledgers/pending-verifications")
    public ResponseEntity<?> getPendingVerifications(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate
    ){
        return new ResponseEntity<>(
                accountService.getAllPendingVerifications(token,page,size,fromDate,toDate),
                HttpStatus.OK);
    }

    @GetMapping("/ledgers/pending-approvals")
    public ResponseEntity<?> getPendingApprovals(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate
    ){
        return new ResponseEntity<>(
                accountService.getAllPendingApprovals(token,page,size, fromDate,toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLedgerDetail(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                accountService.getLedgerDetailById(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/ledgers/approved")
    public ResponseEntity<?> getApprovedLedgerRequests(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("warehouseId") Optional<Long> warehouseId
            ){

        return new ResponseEntity<>(accountService.getAllApprovedAccounts(token, page, size, warehouseId), HttpStatus.OK);

    }

    @GetMapping("/ledgers/rejected")
    public ResponseEntity<?> getRejectedLedgerRequests(@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(accountService.getAllRejectedAccounts(page, size), HttpStatus.OK);
    }
}
