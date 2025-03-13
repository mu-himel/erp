package com.agi.aesl.erpscm.cs.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.cs.dto.CsGetItemWiseVendorsDto;
import com.agi.aesl.erpscm.cs.dto.CsRequestDto;
import com.agi.aesl.erpscm.cs.dto.CsUpdateRequestDto;
import com.agi.aesl.erpscm.cs.enums.CsOperation;
import com.agi.aesl.erpscm.cs.service.CsService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cs")
@RequiredArgsConstructor
public class CsController extends BaseController {

    private final CsService csService;

    @GetMapping
    public ResponseEntity<Object> getAllPendingCs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr
            ){

        return new ResponseEntity<>(
                csService.getAllPendingCs(token,indentNo, status,fromDateStr,toDateStr, page,size),
                HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Void> createCs(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody CsRequestDto csRequestDto
    ){
        csService.createCs(token, uri, csRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCs(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @PathVariable("id") Long id,
            @RequestBody @Valid CsRequestDto csRequestDto
    ){
        csService.updateCsByInitiator(token, uri, id, csRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getCsDetail(@PathVariable("id") Long id){
        return new ResponseEntity<>(csService.getDetailById(id),HttpStatus.OK);
    }

    @GetMapping("/{vendorId}/{csNo}")
    public ResponseEntity<Object> getItemInfoByVendorAndCsNo(
            @PathVariable("vendorId") Long vendorId,
            @PathVariable("csNo") String csNo
    ){
        return new ResponseEntity<>(
                csService.getAllItemsByVendorAndCs(vendorId,csNo),
                HttpStatus.OK
        );
    }

    @PostMapping("/{rfqId}/item-wise-vendors")
    public ResponseEntity<Object> getItemWiseVendors(
            @PathVariable("rfqId") Long id,
            @RequestBody CsGetItemWiseVendorsDto dto
    ){

        return new ResponseEntity<>(
                csService.getItemWiseVendors(id,dto.getBrandName(),dto.getItemName()),
                HttpStatus.OK);
    }

    @PostMapping("/{rfqId}/item-wise-vendors/{vendorId}")
    public ResponseEntity<Object> getItemWiseVendors(
            @PathVariable("rfqId") Long id,
            @PathVariable("vendorId") Long vendorId,
            @RequestBody CsGetItemWiseVendorsDto dto
    ){
        return new ResponseEntity<>(
                csService.getItemWiseVendors(id, vendorId, dto.getItemName()),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}/add-vendor")
    public ResponseEntity<Void> addVendorCs(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody @Valid CsUpdateRequestDto csDto){
        csService.updateCs(token,id,csDto, CsOperation.ADD_VENDOR);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/remove-vendor")
    public ResponseEntity<Void> removeVendorCs(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody @Valid CsUpdateRequestDto csDto
    ){
        csService.updateCs(token,id,csDto,CsOperation.REMOVE_VENDOR);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<Object> getAllPendingVerificationCs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                csService.getPendingVerificationCs(token,
                        indentNo,status,
                        fromDateStr,toDateStr,
                        page, size), HttpStatus.OK);
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<Object> getAllPendingApprovalCs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                csService.getPendingApprovalCs(token,indentNo, status, fromDateStr, toDateStr,page, size), HttpStatus.OK);
    }

    @GetMapping("/approved")
    public ResponseEntity<Object> getAllApprovedCs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                csService.getApprovedCs(token,indentNo,status,fromDateStr,toDateStr,page, size), HttpStatus.OK);
    }

    @GetMapping("/closed")
    public ResponseEntity<Object> getAllClosedCs(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("indentNo") Optional<String> indentNo,
            @RequestParam("status") Optional<String> status,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr,
            @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                csService.getClosedCs(token,indentNo,status,fromDateStr, toDateStr,page, size), HttpStatus.OK);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> rejectCs(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id, @RequestBody @Valid NoteDto noteDto){
        csService.rejectCs(token,id,noteDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<Void> reviewCs(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id, @RequestBody @Valid NoteDto noteDto){
        csService.reviewCs(token, id, noteDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/resent-to-pr")
    public ResponseEntity<Void> resentToPr(@AuthenticationPrincipal Jwt token,
                                        @PathVariable Long id) {
        csService.resentToPr(token,id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/resubmit")
    public ResponseEntity<Void> resubmitForVerification(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id){
            csService.resubmit(token,id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



}
