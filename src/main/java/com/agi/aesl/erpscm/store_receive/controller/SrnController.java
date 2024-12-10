package com.agi.aesl.erpscm.store_receive.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.store_receive.dto.SrnDemandAttrDto;
import com.agi.aesl.erpscm.store_receive.dto.SrnDto;
import com.agi.aesl.erpscm.store_receive.service.SrnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/srn")
public class SrnController  extends BaseController {

    @Autowired
    private SrnService srnService;

    @PostMapping
    public ResponseEntity<?> addSrn(
            @AuthenticationPrincipal Jwt token,
            @RequestBody SrnDto srnDto
    ){
        srnService.addSrn(token,srnDto);
        return new ResponseEntity<>(
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<?> getAllFromQc(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("grnNo") Optional<String> grnNo,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(
                srnService.getAll(token, page,size, grnNo, fromDate, toDate),
                HttpStatus.OK
        );
    }
    @GetMapping("/complete")
    public ResponseEntity<?> getAllComplete(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("grnNo") Optional<String> grnNo,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(
                srnService.getAllComplete(token, page,size, grnNo, fromDate, toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(
            @AuthenticationPrincipal Jwt token,
           @PathVariable("id") Long id){

        return new ResponseEntity<>(
                srnService.getDetail(id),
                HttpStatus.OK
        );
    }

    @GetMapping("demands/{id}")
    public ResponseEntity<?> getPendingDemandListBySrnItems(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                srnService.getPendingDemandListBySrnItems(id),
                HttpStatus.OK
        );
    }

    @PostMapping("/by-attribute")
    public ResponseEntity<?> getPendingDemandListBySrnItems(
            @AuthenticationPrincipal Jwt token,
            @RequestBody SrnDemandAttrDto attributeDto
    ){
        System.out.println(attributeDto.getAttributeName());
        return new ResponseEntity<>(
                srnService.getPendingDemandListBySrnItems(token,attributeDto.getAttributeName()),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-verifications")
    public ResponseEntity<?> getPendingVerifications(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){
        return new ResponseEntity<>(
                srnService.getPendingVerifications(token, fromDate,toDate, page,size),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<?> getPendingApprovals(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                srnService.getPendingApprovals(token, fromDate,toDate, page,size),
                HttpStatus.OK
        );
    }

    @PutMapping("/review/{id}")
    public ResponseEntity<?> review(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
    ){
        srnService.review(token,id,reviewDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }
}

