package com.agi.aesl.erpscm.cs.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.cs.dto.AcsUpdateDto;
import com.agi.aesl.erpscm.cs.service.CsAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/apiv/v1/acs")
public class AcsController extends BaseController {

    @Autowired
    private CsAccountService csAccountService;

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
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
            ){
        return new ResponseEntity<>(csAccountService.getPendingAcs(token,
                indentNo,status,
                page,size), HttpStatus.OK);
    }

    @GetMapping("/approved")
    public ResponseEntity<?> getApprovedAcs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(csAccountService.getApprovedAcs(token,
                indentNo,page,size), HttpStatus.OK);
    }

    @GetMapping("/rejected")
    public ResponseEntity<?> getRejectedAcs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(csAccountService.getRejectedAcs(token,
                indentNo,page,size), HttpStatus.OK);
    }

    @GetMapping("/closed")
    public ResponseEntity<?> getClosedAcs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(csAccountService.getClosedAcs(token,
                indentNo,status,page,size), HttpStatus.OK);
    }

}
