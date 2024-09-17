package com.agi.aesl.erpscm.price_quotation.controller;

import com.agi.aesl.erpscm.price_quotation.dto.request.PriceQuotationReqDto;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStateStatus;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStatus;
import com.agi.aesl.erpscm.price_quotation.service.PqService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pq/vendor")
public class VendorPriceQuotationController {

    @Autowired
    private PqService pqService;

    @PostMapping
    public ResponseEntity<?> onInitialPriceQuotation(
            @AuthenticationPrincipal Jwt token,
            @RequestBody PriceQuotationReqDto pqDto){
        pqService.onReceivePq(token,pqDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/receive-counter")
    public ResponseEntity<?> onReceiveCounter(
            @AuthenticationPrincipal Jwt token,
            @RequestBody PriceQuotationReqDto pqDto){
        pqService.onReceiveCounterPq(token,pqDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<?> onAcceptPriceQuotation(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id
    ){
        pqService.onLockPq(token,id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/decline")
    public ResponseEntity<?> onDeclinePriceQuotation(@PathVariable("id") Long id, @RequestBody NoteDto noteDto){
        pqService.onDeclinePq(id,noteDto, PriceQuotationStateStatus.DECLINED, PriceQuotationStatus.COUNTER_TO_COMPANY);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
