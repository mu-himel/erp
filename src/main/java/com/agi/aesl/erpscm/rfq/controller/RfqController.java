package com.agi.aesl.erpscm.rfq.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.indent.service.IndentService;
import com.agi.aesl.erpscm.rfq.controller.service.RfqService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/rfq")
public class RfqController extends BaseController {


    private RfqService rfqService;

    @GetMapping("/pending")
    public ResponseEntity<?> getAllPendingRFQs(
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
                rfqService.getAllPendingRFQs(token, indentNo, category, subCategory, priority,
                        daysRemain, fromDate, toDate, page, size
                ),
                HttpStatus.OK);
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getAllOpenRfqs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("category") Optional<String> category,
            @RequestParam("subCategory") Optional<String> subCategory,
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
}
