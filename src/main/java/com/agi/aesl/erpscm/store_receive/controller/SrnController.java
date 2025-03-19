package com.agi.aesl.erpscm.store_receive.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.store_receive.dto.SrnDemandAttrDto;
import com.agi.aesl.erpscm.store_receive.dto.SrnDto;
import com.agi.aesl.erpscm.store_receive.service.SrnService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/srn")
@RequiredArgsConstructor
public class SrnController  extends BaseController {


    private final SrnService srnService;

    @PostMapping
    public ResponseEntity<Void> addSrn(
            @AuthenticationPrincipal Jwt token,
            @RequestBody SrnDto srnDto
    ){
        srnService.addSrn(token,srnDto);
        return new ResponseEntity<>(
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<Object> getAllFromQc(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("grnNo") Optional<String> grnNo,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("receivedQty") Optional<Long> receivedQty,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size){
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        return new ResponseEntity<>(
                srnService.getAll(token, pageable, grnNo,categoryId,receivedQty,  fromDate, toDate),
                HttpStatus.OK
        );
    }
    @GetMapping("/complete")
    public ResponseEntity<Object> getAllComplete(
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
    public ResponseEntity<Object> getDetail(
            @AuthenticationPrincipal Jwt token,
           @PathVariable("id") Long id){

        return new ResponseEntity<>(
                srnService.getDetail(id),
                HttpStatus.OK
        );
    }

    @GetMapping("demands/{id}")
    public ResponseEntity<Object> getPendingDemandListBySrnItems(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                srnService.getPendingDemandListBySrnItems(id),
                HttpStatus.OK
        );
    }

    @PostMapping("/by-attribute")
    public ResponseEntity<Object> getPendingDemandListBySrnItems(
            @AuthenticationPrincipal Jwt token,
            @RequestBody SrnDemandAttrDto attributeDto
    ){
        return new ResponseEntity<>(
                srnService.getPendingDemandListBySrnItems(token,attributeDto.getAttributeName()),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-verifications")
    public ResponseEntity<Object> getPendingVerifications(
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
    public ResponseEntity<Object> getPendingApprovals(
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
    public ResponseEntity<Void> review(
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

