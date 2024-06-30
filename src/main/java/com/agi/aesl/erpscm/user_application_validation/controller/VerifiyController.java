package com.agi.aesl.erpscm.user_application_validation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.service.DemandService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.ApproveDto;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;


@RequestMapping("/api/v1/test")
public class VerifiyController extends BaseController{

    @Autowired
    private UserApplicationValidatorService verificationService;

    @Autowired
    private DemandService demandService;
    
 
    @PostMapping("/approve")
    public ResponseEntity<?> approve(@RequestBody ApproveDto approveDto){
        if(approveDto.getDomainType().equals(DomainType.DEMAND)){
            verificationService.setVerificationDomainService(demandService);
        }
        verificationService.approve(approveDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
