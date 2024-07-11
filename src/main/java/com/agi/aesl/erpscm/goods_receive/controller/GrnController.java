package com.agi.aesl.erpscm.goods_receive.controller;

import com.agi.aesl.erpscm.goods_receive.dto.request.GoodReceiveNoteDto;
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
}
