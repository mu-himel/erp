package com.agi.aesl.erpscm.account_finance.controller;

import com.agi.aesl.erpscm.account_finance.dto.request.LedgerAccountRequestDto;
import com.agi.aesl.erpscm.account_finance.service.AccountService;
import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountFinanceController extends BaseController {


    private final AccountService accountService;

    record LedgerRequest(Long id, String assetNo, String store, String category, String subCategory, String product,
                         String group, String status){}


    @PutMapping("/{id}")
    public ResponseEntity<Void> updateLedgerAccount(
            @RequestHeader("uri") String uri,
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id, @RequestBody LedgerAccountRequestDto ledgerAccountRequestDto){
        accountService.updateAccount(token, uri,  id, ledgerAccountRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/review/{id}")
    public ResponseEntity<Void> review(
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
    public ResponseEntity<Object> getLedgerRequests(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("warehouseId") Optional<Long> warehouseId
            ){
        return new ResponseEntity<>(accountService.getAllPendingAccounts(token,page,size,warehouseId), HttpStatus.OK);
    }

    @GetMapping("/ledgers/closed")
    public ResponseEntity<Object> getClosedLedgerRequests(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("warehouseId") Optional<Long> warehouseId

    ){
        return new ResponseEntity<>(
                accountService.getClosedAccounts(token,
                    page,size,
                    warehouseId),
                HttpStatus.OK);
    }

    @GetMapping("/ledgers/pending-verifications")
    public ResponseEntity<Object> getPendingVerifications(
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
    public ResponseEntity<Object> getPendingApprovals(
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
    public ResponseEntity<Object> getLedgerDetail(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                accountService.getLedgerDetailById(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/ledgers/approved")
    public ResponseEntity<Object> getApprovedLedgerRequests(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("warehouseId") Optional<Long> warehouseId
            ){

        return new ResponseEntity<>(accountService.getAllApprovedAccounts(token, page, size, warehouseId), HttpStatus.OK);

    }

    @GetMapping("/ledgers/rejected")
    public ResponseEntity<Object> getRejectedLedgerRequests(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("warehouseId") Optional<Long> warehouseId){
        return new ResponseEntity<>(
                accountService.getAllRejectedAccounts(token, page, size, warehouseId),
                HttpStatus.OK);
    }
}
