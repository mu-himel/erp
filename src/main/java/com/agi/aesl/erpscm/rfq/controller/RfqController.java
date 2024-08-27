package com.agi.aesl.erpscm.rfq.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.indent.service.IndentService;
import com.agi.aesl.erpscm.rfq.controller.service.RfqService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
                rfqService.getAllPendingRFQs(indentNo, category, subCategory, priority,
                        daysRemain, fromDate, toDate, page, size
                ),
                HttpStatus.OK);
    }
}
