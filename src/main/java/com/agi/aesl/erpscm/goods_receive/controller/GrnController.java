package com.agi.aesl.erpscm.goods_receive.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.goods_receive.dto.request.GoodReceiveNoteDto;

import com.agi.aesl.erpscm.goods_receive.dto.request.GrnManualRequestDto;

import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
public class GrnController extends BaseController {

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
            @RequestBody @Valid GrnManualRequestDto grnManualDto
    ){
        grnService.createManualGrn(token, grnManualDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }



    @GetMapping("/all")
    public ResponseEntity<?> getAllGrn(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("grnNo") Optional<String> grnNo,
            @RequestParam("itemQty") Optional<Integer> qty,
            @RequestParam("receivedQty") Optional<Integer> receivedQty,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                grnService.getAllGrn(token,page,size,grnNo,qty, receivedQty,fromDate,toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGrnById(
            @PathVariable("id") Long id
    ){
        return new ResponseEntity<>(grnService.getGrnById(id,true),HttpStatus.OK);
    }
}
