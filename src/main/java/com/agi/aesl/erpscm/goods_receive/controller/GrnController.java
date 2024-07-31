package com.agi.aesl.erpscm.goods_receive.controller;

import com.agi.aesl.erpscm.goods_receive.dto.request.GoodReceiveNoteDto;
import com.agi.aesl.erpscm.goods_receive.dto.request.GrnManualDto;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/goods-receive-note")
public class GrnController {

    @Autowired
    private GrnService grnService;

    @GetMapping("/next-id")
    public ResponseEntity<?> getNextGrnNumber(){
        Map<String,Object> response = new HashMap<>();
        response.put("code", grnService.getNextGrnNumber());
        return new ResponseEntity<>(
                response, HttpStatus.OK
        );
    }

    @PostMapping
    public ResponseEntity<?> createGrn(
            @AuthenticationPrincipal Jwt token,
            @RequestBody @Valid GoodReceiveNoteDto goodReceiveNoteDto
            ){
        grnService.addGrn(token,goodReceiveNoteDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/manual")
    public ResponseEntity<?> createManualGrn(
            @AuthenticationPrincipal Jwt token,
            @RequestBody @Valid GrnManualDto grnManualDto
    ){
        grnService.createGrn(grnManualDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/available-vendors")
    public ResponseEntity<?> getAvailableVendors(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("name") Optional<String> name){
        return new ResponseEntity<>(
                grnService.getAvailableVendors(token, name),
                HttpStatus.OK
        );
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllGrn(
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
            ){
        return new ResponseEntity<>(
                grnService.getAllGrn(page,size,fromDate,toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGrnById(
            @PathVariable("id") Long id
    ){
        return new ResponseEntity<>(grnService.getGrnById(id,false),HttpStatus.OK);
    }
}
