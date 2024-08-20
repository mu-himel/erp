package com.agi.aesl.erpscm.pr_indent.controller;

import com.agi.aesl.erpscm.pr_indent.dto.reqeust.PrIndentRequestDto;
import com.agi.aesl.erpscm.pr_indent.dto.reqeust.UpdatePrIndentDetailRequestDto;
import com.agi.aesl.erpscm.pr_indent.service.PrIndentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/pr-indents")
public class PrIndentController {

    @Autowired
    private PrIndentService prIndentService;

    public ResponseEntity<?> addIndent(
            @AuthenticationPrincipal Jwt token,
            @RequestBody @Valid PrIndentRequestDto prIndentRequestDto
            ){
        prIndentService.createPrIndent(token, prIndentRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "Get PR Indent with Pagination")
    public ResponseEntity<?> getIndents(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page")Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("startDate") Optional<String> priority){

        return new ResponseEntity<>(
                prIndentService.getAllPrIndents(page, size, categoryId, subCategoryId, priority),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPrIndentById(
            @PathVariable("id") Optional<Long> id
    ){
        return new ResponseEntity<>(
                prIndentService.getPrIndentById(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/getByIds")
    public ResponseEntity<?> getPrIndentByIds(
            @RequestParam("ids") Optional<List<Long>> ids
    ){
        return new ResponseEntity<>(
                prIndentService.getPrIndentByIds(ids),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetailsOrderQty(
            @PathVariable("id") @Valid Optional<Long> id,
            @RequestBody @Valid UpdatePrIndentDetailRequestDto updatePrIndentDetailRequestDto
            ){
        prIndentService.updateOrderDetailsOrderQty(updatePrIndentDetailRequestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
