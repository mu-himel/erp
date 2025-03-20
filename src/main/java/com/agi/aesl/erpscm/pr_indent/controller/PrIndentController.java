package com.agi.aesl.erpscm.pr_indent.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.pr_indent.dto.reqeust.PrIndentRequestDto;
import com.agi.aesl.erpscm.pr_indent.dto.reqeust.UpdatePrIndentDetailRequestDto;
import com.agi.aesl.erpscm.pr_indent.service.PrIndentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/pr-indents")
@RequiredArgsConstructor
public class PrIndentController extends BaseController {

    private final PrIndentService prIndentService;

    @PostMapping
    public ResponseEntity<Void> addIndent(
            @AuthenticationPrincipal Jwt token,
            @RequestBody PrIndentRequestDto prIndentRequestDto
            ){
        prIndentService.createPrIndent(token, prIndentRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Object> getIndents(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page")Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate){

        return new ResponseEntity<>(
                prIndentService.getAllPrIndents(page, size, categoryId, subCategoryId, fromDate,toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getPrIndentById(
            @PathVariable("id") Optional<Long> id
    ){
        return new ResponseEntity<>(
                prIndentService.getPrIndentById(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/getByIds")
    public ResponseEntity<Object> getPrIndentByIds(
            @RequestParam("ids") Optional<List<Long>> ids
    ){
        return new ResponseEntity<>(
                prIndentService.getPrIndentByIds(ids),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateOrderDetailsOrderQty(
            @PathVariable("id") @Valid Optional<Long> id,
            @RequestBody @Valid UpdatePrIndentDetailRequestDto updatePrIndentDetailRequestDto
            ){
        prIndentService.updateOrderDetailsOrderQty(updatePrIndentDetailRequestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
