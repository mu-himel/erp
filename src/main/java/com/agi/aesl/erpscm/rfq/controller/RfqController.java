package com.agi.aesl.erpscm.rfq.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.rfq.dto.RfqRequestDto;
import com.agi.aesl.erpscm.rfq.service.service.RfqService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/rfq")
public class RfqController extends BaseController {


    @Autowired
    private RfqService rfqService;

    @PostMapping
    public ResponseEntity<?> createRfQ(
            @AuthenticationPrincipal Jwt token,
            @Valid @RequestBody RfqRequestDto requestDto){
        rfqService.createRfq(token,requestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getAllPendingRFQs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam Optional<String> indentNo,
            @RequestParam Optional<Long> categoryId,
            @RequestParam Optional<Long> subCategoryId,
            @RequestParam Optional<String> priority,
            @RequestParam Optional<Integer> daysRemain,
            @RequestParam Optional<String> fromDate,
            @RequestParam Optional<String> toDate,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size
    ){

        return new ResponseEntity<>(
                rfqService.getAllPendingRFQs(token, indentNo, categoryId, subCategoryId, priority,
                        daysRemain, fromDate, toDate, page, size
                ),
                HttpStatus.OK);
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getAllOpenRfqs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("categoryId") Optional<Long> category,
            @RequestParam("subCategoryId") Optional<Long> subCategory,
            @RequestParam("priority") Optional<String> priority,
            @RequestParam("daysRemain") Optional<Integer> daysRemain,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
            ){
        return new ResponseEntity<>(
                rfqService.getAllSentRfqs(
                        token, indentNo, category,subCategory,
                        priority,daysRemain,fromDate,toDate,
                        page,size
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/closed")
    public ResponseEntity<?> getClosedRfqs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam Optional<String> indentNo,
            @RequestParam Optional<String> category,
            @RequestParam Optional<String> subCategory,
            @RequestParam Optional<String> priority,
            @RequestParam Optional<Integer> daysRemain,
            @RequestParam Optional<String> fromDate,
            @RequestParam Optional<String> toDate,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size
    ){
        return new ResponseEntity<>(
                rfqService.getAllClosedRFQs(
                        token,
                        indentNo,category,subCategory,priority,daysRemain,
                        fromDate,toDate, page,size
                ),
                HttpStatus.OK);
    }

    @GetMapping("/{id}/get-vendors-count")
    public ResponseEntity<?> getAvailableVendorsCount(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id
    ) {

        return new ResponseEntity<>(rfqService.getAvailableVendorsCount(token,id),HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> expire(@AuthenticationPrincipal Jwt token,
                                    @PathVariable("id") Long id){
        rfqService.expire(token,id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
