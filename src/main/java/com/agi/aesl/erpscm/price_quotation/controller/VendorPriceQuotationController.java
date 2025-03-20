package com.agi.aesl.erpscm.price_quotation.controller;

import com.agi.aesl.erpscm.price_quotation.dto.request.PriceQuotationReqDto;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStateStatus;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStatus;
import com.agi.aesl.erpscm.price_quotation.service.PqService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pq/vendor")
@RequiredArgsConstructor
public class VendorPriceQuotationController {


    private final PqService pqService;

    @PostMapping
    public ResponseEntity<Void> onInitialPriceQuotation(
            @AuthenticationPrincipal Jwt token,
            @RequestBody PriceQuotationReqDto pqDto){
        pqService.onReceivePq(token,pqDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/receive-counter")
    public ResponseEntity<Void> onReceiveCounter(
            @AuthenticationPrincipal Jwt token,
            @RequestBody PriceQuotationReqDto pqDto){
        pqService.onReceiveCounterPq(token,pqDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<Void> onAcceptPriceQuotation(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id
    ){
        pqService.onLockPq(token,id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/decline")
    public ResponseEntity<Void> onDeclinePriceQuotation(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id, @RequestBody NoteDto noteDto){
        pqService.onDeclinePq(token, id,noteDto, PriceQuotationStateStatus.DECLINED, PriceQuotationStatus.COUNTER_TO_COMPANY);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
