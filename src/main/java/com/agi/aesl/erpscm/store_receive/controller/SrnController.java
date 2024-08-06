package com.agi.aesl.erpscm.store_receive.controller;

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
public class SrnController {

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
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(
                srnService.getAll(page,size, fromDate, toDate),
                HttpStatus.OK
        );
    }
    @GetMapping("/complete")
    public ResponseEntity<?> getAllComplete(
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(
                srnService.getAllComplete(page,size, fromDate, toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPendingDemandListBySrnItems(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                srnService.getPendingDemandListBySrnItems(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/by-attribute/{attributes}")
    public ResponseEntity<?> getPendingDemandListBySrnItems(
            @PathVariable("attributes") String attributes
    ){
        System.out.println(attributes);
        return new ResponseEntity<>(
                srnService.getPendingDemandListBySrnItems(attributes),
                HttpStatus.OK
        );
    }
}

