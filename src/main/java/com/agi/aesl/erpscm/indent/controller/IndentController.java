package com.agi.aesl.erpscm.indent.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.indent.dto.request.IndentRequestDto;
import com.agi.aesl.erpscm.indent.service.IndentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/indents")
public class IndentController extends BaseController {

    @Autowired
    private IndentService indentService;

    @PostMapping
    public ResponseEntity<?> addIndent(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody @Valid IndentRequestDto indentRequestDto
            ){
        indentService.createIndent(token,uri,indentRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
