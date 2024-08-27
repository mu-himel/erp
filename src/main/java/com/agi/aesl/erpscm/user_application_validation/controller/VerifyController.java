package com.agi.aesl.erpscm.user_application_validation.controller;

import com.agi.aesl.erpscm.account_finance.service.AccountService;
import com.agi.aesl.erpscm.indent.service.IndentService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.VerifyDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.service.DemandService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.ApproveDto;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;


@RestController
@RequestMapping("/api/v1/verify")
public class VerifyController extends BaseController{

    @Autowired
    private UserApplicationValidatorService<?> verificationService;

    @Autowired
    private DemandService demandService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private IndentService indentService;

    @PutMapping("/approve")
    public ResponseEntity<?> approve(
            @AuthenticationPrincipal Jwt token,
            @RequestBody ApproveDto approveDto){
        if(approveDto.getDomainType().equals(DomainType.DEMAND)){
            verificationService.setVerificationDomainService(demandService);
            verificationService.approve(token, approveDto);
        }
        if(approveDto.getDomainType().equals(DomainType.ACCOUNT_LEDGER)){
            verificationService.setVerificationDomainService(accountService);
            verificationService.approve(token, approveDto);
        }
        if(approveDto.getDomainType().equals(DomainType.INDENT)){
            verificationService.setVerificationDomainService(indentService);
            verificationService.approve(token, approveDto);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/verify")
    public ResponseEntity<?> verify(
            @AuthenticationPrincipal Jwt token,
            @RequestBody VerifyDto verifyDto){
        if(verifyDto.getDomainType().equals(DomainType.DEMAND)){
            verificationService.setVerificationDomainService(demandService);
            verificationService.verify(token, verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.ACCOUNT_LEDGER)){
            verificationService.setVerificationDomainService(accountService);
            verificationService.verify(token, verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.INDENT)){
            verificationService.setVerificationDomainService(indentService);
            verificationService.verify(token, verifyDto);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/review")
    public ResponseEntity<?> review(@RequestBody VerifyDto verifyDto){
        if(verifyDto.getDomainType().equals(DomainType.DEMAND)){
            verificationService.setVerificationDomainService(demandService);
            verificationService.review(verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.ACCOUNT_LEDGER)){
            verificationService.setVerificationDomainService(accountService);
            verificationService.review(verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.INDENT)){
            verificationService.setVerificationDomainService(indentService);
            verificationService.review(verifyDto);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/reject")
    public ResponseEntity<?> reject(
            @AuthenticationPrincipal Jwt token,
            @RequestBody RejectDto rejectDto){

        if(rejectDto.getDomainType().equals(DomainType.ACCOUNT_LEDGER)){
            verificationService.setVerificationDomainService(accountService);
            verificationService.reject(token, rejectDto);
        }
        if(rejectDto.getDomainType().equals(DomainType.INDENT)){
            verificationService.setVerificationDomainService(indentService);
            verificationService.reject(token, rejectDto);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
