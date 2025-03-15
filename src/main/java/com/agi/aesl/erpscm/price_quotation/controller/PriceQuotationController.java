package com.agi.aesl.erpscm.price_quotation.controller;


import com.agi.aesl.erpscm.common.BaseController;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/pq")
@RequiredArgsConstructor
public class PriceQuotationController extends BaseController {


    private final PqService pqService;

    @PostMapping("/add")
    public ResponseEntity<Void> addPriceQuotation(
            @AuthenticationPrincipal Jwt token,
            @RequestBody PriceQuotationReqDto pqDto
            ){
        pqService.addManualPq(token,pqDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getPriceQuotationDetail(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                pqService.getDetail(id),
                HttpStatus.OK);
    }

    @GetMapping("/rfq/{id}")
    public ResponseEntity<Object> getAllPriceQuotationsByRfq(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                pqService.getPriceQuotationsByIndent(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/rfq/{id}/negotiation-history/{vendorId}")
    public ResponseEntity<Object> getNegotiationHistories(@PathVariable("id") Long id,
                                                     @PathVariable("vendorId") Long vendorId){

        return new ResponseEntity<>(
                pqService.getHistoriesByRfq(id,vendorId),
                HttpStatus.OK);
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<Void> lockPriceQuotation(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id) {
        pqService.lockPq(token,id, PriceQuotationStateStatus.LOCKED);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/recommended-for-cs")
    public ResponseEntity<Void> recommendPq(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id){
        pqService.recommendPq(token, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/decline")
    public ResponseEntity<Void> declinePq(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody NoteDto noteDto
            ){
        pqService.onDeclinePq(token, id,noteDto,PriceQuotationStateStatus.DECLINED, PriceQuotationStatus.COUNTER_TO_VENDOR);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/sent-counter")
    public ResponseEntity<Void> sentCounterPq(
            @AuthenticationPrincipal Jwt token,
            @RequestBody PriceQuotationReqDto pqDto
    ){
        pqService.sendPq(token,pqDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<Object> uploadDoc(@RequestPart MultipartFile file, @PathVariable("id") Long id) {
        return new ResponseEntity<>(
                pqService.uploadDoc(id,file),
                HttpStatus.CREATED
        );
    }
}
