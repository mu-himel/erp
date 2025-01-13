package com.agi.aesl.erpscm.cs.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.cs.dto.AcsUpdateDto;
import com.agi.aesl.erpscm.cs.service.CsAccountService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/acs")
public class AcsController extends BaseController {

    @Autowired
    private CsAccountService csAccountService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                csAccountService.getDetailById(id),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> submitForVerifyApproval(
            @PathVariable("id") Long id,
            @RequestHeader("uri") String uri,
            @AuthenticationPrincipal Jwt token,
            @RequestBody AcsUpdateDto acsUpdateDto
            ){
        csAccountService.updateCsAccount(id,token, uri, acsUpdateDto );
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingAcs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
            ){
        return new ResponseEntity<>(csAccountService.getPendingAcs(token,
                indentNo,status, fromDateStr, toDateStr,
                page,size), HttpStatus.OK);
    }

    @GetMapping("/pending-verifications")
    public ResponseEntity<?> getPendingVerificationAcs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(csAccountService.getPendingVerificationAcs(token,
                indentNo,status,fromDateStr,toDateStr,
                page,size), HttpStatus.OK);
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<?> getPendingApprovalAcs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(csAccountService.getPendingApprovalAcs(token,
                indentNo,status, fromDate, toDate,
                page,size), HttpStatus.OK);
    }

    @GetMapping("/approved")
    public ResponseEntity<?> getApprovedAcs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(csAccountService.getApprovedAcs(token,
                indentNo,status,fromDateStr,toDateStr,page,size), HttpStatus.OK);
    }

    @GetMapping("/rejected")
    public ResponseEntity<?> getRejectedAcs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(csAccountService.getRejectedAcs(token,
                indentNo,status,fromDateStr,toDateStr,page,size), HttpStatus.OK);
    }

    @GetMapping("/closed")
    public ResponseEntity<?> getClosedAcs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(csAccountService.getClosedAcs(token,
                indentNo,status,fromDateStr,toDateStr,page,size), HttpStatus.OK);
    }

    @GetMapping("/active-cs")
    public ResponseEntity<?> getActiveAcs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(csAccountService.getActiveCsList(token,
                indentNo,page,size), HttpStatus.OK);
    }

    @GetMapping("/expired-cs")
    public ResponseEntity<?> getExpiredAcs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){
        return new ResponseEntity<>(csAccountService.getExpiredCsList(token,
                indentNo,page,size), HttpStatus.OK);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<?> reviewCs(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id, @RequestBody @Valid NoteDto noteDto){
        csAccountService.reviewAcs(token, id, noteDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
