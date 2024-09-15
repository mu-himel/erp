package com.agi.aesl.erpscm.cs.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.cs.service.CsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
