package com.agi.aesl.erpscm.organization.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agi.aesl.erpscm.organization.dto.request.OrgRequestDto;
import com.agi.aesl.erpscm.organization.service.OrgService;

@RestController
@RequestMapping("/api/v1/organization")
@RequiredArgsConstructor
public class OrganizationController {


    private final OrgService orgService;

    @PostMapping
    public ResponseEntity<Void> createOrg(@RequestBody OrgRequestDto orgDto){
        orgService.createOrg(orgDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    
}
