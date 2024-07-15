package com.agi.aesl.erpscm.account_finance.controller;

import com.agi.aesl.erpscm.account_finance.dto.request.LedgerAccountRequestDto;
import com.agi.aesl.erpscm.account_finance.service.AccountService;
import com.agi.aesl.erpscm.common.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> updateLedgerAccount(@PathVariable("id") Long id, @RequestBody LedgerAccountRequestDto ledgerAccountRequestDto){
        accountService.updateAccount(id, ledgerAccountRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



    @GetMapping("/ledgers")
    public ResponseEntity<?> getLedgerRequests(
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
            ){
        return new ResponseEntity<>(accountService.getAllPendingAccounts(page,size), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLedgerDetail(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                accountService.getLedgerDetailById(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/ledgers/approved")
    public ResponseEntity<?> getApprovedLedgerRequests(@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size){
        /**TODO for Sourav -> Please replace commented out code with actual result
         * Set null with actual value
        */
        return new ResponseEntity<>(accountService.getAllApprovedAccounts(page, size), HttpStatus.OK);

    }

    @GetMapping("/ledgers/rejected")
    public ResponseEntity<?> getRejectedLedgerRequests(@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(accountService.getAllRejectedAccounts(page, size), HttpStatus.OK);
    }
}
