package com.agi.aesl.erpscm.cs.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.cs.dto.CsGetItemWiseVendorsDto;
import com.agi.aesl.erpscm.cs.dto.CsRequestDto;
import com.agi.aesl.erpscm.cs.service.CsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cs")
public class CsController extends BaseController {

    @Autowired
    private CsService csService;

    @GetMapping
    public ResponseEntity<?> getAllPendingCs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
            ){

        return new ResponseEntity<>(
                csService.getAllPendingCs(token,page,size),
                HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createCs(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody CsRequestDto csRequestDto
    ){
        csService.createCs(token, uri, csRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCs(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @PathVariable("id") Long id,
            @RequestBody @Valid CsRequestDto csRequestDto
    ){
        csService.updateCsByInitiator(token, uri, id, csRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCsDetail(@PathVariable("id") Long id){
        return new ResponseEntity<>(csService.getDetailById(id),HttpStatus.OK);
    }

    @PostMapping("/{rfqId}/item-wise-vendors")
    public ResponseEntity<?> getItemWiseVendors(
            @PathVariable("rfqId") Long id,
            @RequestBody CsGetItemWiseVendorsDto dto
    ){

        return new ResponseEntity<>(
                csService.getItemWiseVendors(id,dto.getItemName()),
                HttpStatus.OK);
    }

    @PostMapping("/{rfqId}/item-wise-vendors/{vendorId}")
    public ResponseEntity<?> getItemWiseVendors(
            @PathVariable("rfqId") Long id,
            @PathVariable("vendorId") Long vendorId,
            @RequestBody CsGetItemWiseVendorsDto dto
    ){
        return new ResponseEntity<>(
                csService.getItemWiseVendors(id, vendorId, dto.getItemName()),
                HttpStatus.OK
        );
    }



}
