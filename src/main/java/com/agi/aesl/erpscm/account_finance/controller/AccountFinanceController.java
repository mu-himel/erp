package com.agi.aesl.erpscm.account_finance.controller;

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


    @GetMapping("/next-id")
    public ResponseEntity<?> getNextNumber(){
        return new ResponseEntity<>(
                accountService,
                HttpStatus.OK
        );
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
    public ResponseEntity<?> getApprovedLedgerRequests(){
//        List<LedgerRequest> ledgerRequestList = new ArrayList<>();
//        LedgerRequest lr = new LedgerRequest(2L,"A-10001","Store","Category","SubCategory",
//                "Product-01","Group","Approved");
//        ledgerRequestList.add(lr);
//        Pageable pageable = PageRequest.of(0,10);
//        Page<?> page = new PageImpl<LedgerRequest>(ledgerRequestList,pageable,10);
        /**TODO for Sourav -> Please replace commented out code with actual result
         * Set null with actual value
        */
        return new ResponseEntity<>(null, HttpStatus.OK);

    }

    @GetMapping("/ledgers/rejected")
    public ResponseEntity<?> getRejectedLedgerRequests(){
//        List<LedgerRequest> ledgerRequestList = new ArrayList<>();
//        LedgerRequest lr = new LedgerRequest(1L,"A-10001","Store","Category",
//                "SubCategory","Product-01","Group","Rejected");
//        ledgerRequestList.add(lr);
//        Pageable pageable = PageRequest.of(0,10);
//        Page<?> page = new PageImpl<LedgerRequest>(ledgerRequestList,pageable,10);
        /**TODO for Sourav -> Please replace commented out code with actual result
         * Set null with actual value
         */
        return new ResponseEntity<>(null, HttpStatus.OK);

    }
}
