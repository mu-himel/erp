package com.agi.aesl.erpscm.account_finance.controller;

import com.agi.aesl.erpscm.common.BaseController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountFinanceController extends BaseController {

    record LedgerRequest(Long id, String assetNo, String store, String category, String subCategory, String product,
                         String group){}

    @GetMapping("/ledgers")
    public ResponseEntity<?> getLedgerRequests(){
        List<LedgerRequest> ledgerRequestList = new ArrayList<>();
        LedgerRequest lr = new LedgerRequest(3L,"A-10001","Store","Category","SubCategory","Product-01","Group");
        ledgerRequestList.add(lr);
        Pageable pageable = PageRequest.of(0,10);
        Page<?> page = new PageImpl<LedgerRequest>(ledgerRequestList,pageable,10);
        return new ResponseEntity<>(page, HttpStatus.OK);

    }

    @GetMapping("/ledgers/approved")
    public ResponseEntity<?> getApprovedLedgerRequests(){
        List<LedgerRequest> ledgerRequestList = new ArrayList<>();
        LedgerRequest lr = new LedgerRequest(2L,"A-10001","Store","Category","SubCategory","Product-01","Group");
        ledgerRequestList.add(lr);
        Pageable pageable = PageRequest.of(0,10);
        Page<?> page = new PageImpl<LedgerRequest>(ledgerRequestList,pageable,10);
        return new ResponseEntity<>(page, HttpStatus.OK);

    }

    @GetMapping("/ledgers/rejected")
    public ResponseEntity<?> getRejectedLedgerRequests(){
        List<LedgerRequest> ledgerRequestList = new ArrayList<>();
        LedgerRequest lr = new LedgerRequest(1L,"A-10001","Store","Category","SubCategory","Product-01","Group");
        ledgerRequestList.add(lr);
        Pageable pageable = PageRequest.of(0,10);
        Page<?> page = new PageImpl<LedgerRequest>(ledgerRequestList,pageable,10);
        return new ResponseEntity<>(page, HttpStatus.OK);

    }
}
