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

import java.util.Optional;

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

    @GetMapping
    public ResponseEntity<?> getIndents(@AuthenticationPrincipal Jwt token,
                                        @RequestParam("page") Optional<Integer> page,
                                        @RequestParam("size") Optional<Integer> size,
                                        @RequestParam("categoryId") Optional<Long> categoryId,
                                        @RequestParam("subCategoryId") Optional<Long> subCategoryId,
                                        @RequestParam("startDate") Optional<String> priority){

        return new ResponseEntity<>(
            indentService.getAllIndents(token, page, size, categoryId, subCategoryId, priority),
            HttpStatus.OK
        );
    }

    @GetMapping("/closed")
    public ResponseEntity<?> closedIndents(
        @AuthenticationPrincipal Jwt token,
        @RequestParam("page") Optional<Integer> page,
        @RequestParam("size") Optional<Integer> size,
        @RequestParam("categoryId") Optional<Long> categoryId,
        @RequestParam("subCategoryId") Optional<Long> subCategoryId,
        @RequestParam("startDate") Optional<String> priority
    ){
        return new ResponseEntity<>(
                indentService.getAllClosedIndents(token,page,size,categoryId,subCategoryId,priority),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIndentById(
            @PathVariable("id") Long id
    ){
        return new ResponseEntity<>(
                indentService.getIndentById(id),
                HttpStatus.OK
        );
    }

    
}
