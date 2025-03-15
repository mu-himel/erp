package com.agi.aesl.erpscm.indent.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.indent.dto.request.IndentRequestDto;
import com.agi.aesl.erpscm.indent.service.IndentService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/indents")
public class IndentController extends BaseController {

    @Autowired
    private IndentService indentService;

    @GetMapping("/next-id")
    public ResponseEntity<?> getNextId(){
        Map<String,Object> code = new HashMap<>();
        code.put("code",indentService.getNextIndentNo());
        return new ResponseEntity<>(code,HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> addIndent(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody @Valid IndentRequestDto indentRequestDto
            ){
        indentService.createIndent(token,uri,indentRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateIndent(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody @Valid IndentRequestDto indentRequestDto
    ){
        indentService.updateIndent(token, uri,id, indentRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/getByIds")
    public ResponseEntity<?> getIndentByIds(
            @RequestParam("indentIds") Optional<List<Long>> indentIds
    ) {

        return new ResponseEntity<>(
                indentService.getIndentByIds(indentIds),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<?> getIndents(@AuthenticationPrincipal Jwt token,
                                        @RequestParam("page") Optional<Integer> page,
                                        @RequestParam("size") Optional<Integer> size,
                                        @RequestParam("categoryId") Optional<Long> categoryId,
                                        @RequestParam("subCategoryId") Optional<Long> subCategoryId,
                                        @RequestParam("fromDate") Optional<String> fromDate,
                                        @RequestParam("toDate") Optional<String> toDate,
                                        @RequestParam("indentNo") Optional<String> indentNo
                                        ){

        return new ResponseEntity<>(
            indentService.getAllIndents(token, page, size, categoryId, subCategoryId, fromDate,toDate,indentNo),
            HttpStatus.OK
        );
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<?> getPendingVerificationIndents(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("indentNo") Optional<String> indentNo
    ){
        return new ResponseEntity<>(
                indentService.getAllPendingVerificationIndents(token,categoryId,subCategoryId, fromDate,toDate ,
                        indentNo, page, size),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<?> getPendingApprovalIndents(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("indentNo") Optional<String> indentNo
    ){
        return new ResponseEntity<>(
                indentService.getAllPendingApprovalIndents(
                        token,
                        categoryId,subCategoryId,
                        fromDate,toDate,indentNo, page, size),
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
        @RequestParam("startDate") Optional<String> priority,
        @RequestParam("indentNo") Optional<String> indentNo
    ){
        return new ResponseEntity<>(
                indentService.getAllClosedIndents(token,page,size,categoryId,subCategoryId,priority,indentNo),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIndentById(
            @PathVariable("id") Long id
    ){
        return new ResponseEntity<>(
                indentService.getIndentDetailById(id),
                HttpStatus.OK
        );
    }

    @PostMapping("/review/{id}")
    public ResponseEntity<?> review(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
            ){
        indentService.reviewIndent(token,id,reviewDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }





    
}
