package com.agi.aesl.erpscm.quality_control.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.quality_control.dto.request.QcDto;
import com.agi.aesl.erpscm.quality_control.service.QcService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/qc")
@RequiredArgsConstructor
public class QcController extends BaseController {


    private final QcService qcService;


    private final GrnService grnService;

    @GetMapping
    public ResponseEntity<?> getAllForQc(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("grnNo") Optional<String> grnNo,
            @RequestParam("items") Optional<Integer> qty,
            @RequestParam("grnMode") Optional<String> grnMode,
            @RequestParam("poNo") Optional<String> poNo,
            @RequestParam("receivedQty") Optional<Integer> receivedQty,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(
                grnService.getAllGrnPendingQC(token, page,
                        size, grnNo, qty,grnMode,poNo,
                        receivedQty, fromDate, toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-verifications")
    public ResponseEntity<?> getAllPendingVerifications(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("grnNo") Optional<String> grnNo,
            @RequestParam("items") Optional<Integer> qty,
            @RequestParam("receivedQty") Optional<Integer> receivedQty,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                qcService.getAllPendingVerificationQC(token, page,
                        size, grnNo, qty,
                        receivedQty, fromDate, toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<?> getAllPendingApprovals(@AuthenticationPrincipal Jwt token,
                                                    @RequestParam("grnNo") Optional<String> grnNo,
                                                    @RequestParam("items") Optional<Integer> qty,
                                                    @RequestParam("receivedQty") Optional<Integer> receivedQty,
                                                    @RequestParam("fromDate") Optional<String> fromDate,
                                                    @RequestParam("toDate") Optional<String> toDate,
                                                    @RequestParam("page") Optional<Integer> page,
                                                    @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(
                qcService.getAllPendingApprovalQC(token, page,
                        size, grnNo, qty,
                        receivedQty, fromDate, toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/complete")
    public ResponseEntity<?> getAllClosed(@AuthenticationPrincipal Jwt token,
                                                    @RequestParam("grnNo") Optional<String> grnNo,
                                                    @RequestParam("items") Optional<Integer> qty,
                                                    @RequestParam("receivedQty") Optional<Integer> receivedQty,
                                                    @RequestParam("fromDate") Optional<String> fromDate,
                                                    @RequestParam("toDate") Optional<String> toDate,
                                                    @RequestParam("page") Optional<Integer> page,
                                                    @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(
                qcService.getAllClosed(token, page,
                        size, grnNo, qty,
                        receivedQty, fromDate, toDate),
                HttpStatus.OK
        );
    }

    @PutMapping("/review/{id}")
    public ResponseEntity<?> review(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
    ){
        qcService.review(token,id,reviewDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @GetMapping("/rejected")
    public ResponseEntity<?> getAllRejected(@AuthenticationPrincipal Jwt token,
                                          @RequestParam("grnNo") Optional<String> grnNo,
                                          @RequestParam("items") Optional<Integer> qty,
                                          @RequestParam("receivedQty") Optional<Integer> receivedQty,
                                          @RequestParam("fromDate") Optional<String> fromDate,
                                          @RequestParam("toDate") Optional<String> toDate,
                                          @RequestParam("page") Optional<Integer> page,
                                          @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(
                qcService.getAllRejected(token, page,
                        size, grnNo, qty,
                        receivedQty, fromDate, toDate),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ResponseEntity<?> addQc(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody QcDto qualityControlDto) throws IllegalAccessException {
        qcService.setGrnService(grnService);
        qcService.addQc(token, uri, qualityControlDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectQc(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody NoteDto noteDto
            ){
        qcService.setGrnService(grnService);
        qcService.rejectQc(token,id, noteDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQcDetail(@PathVariable("id") Long id){
        qcService.setGrnService(grnService);
        return new ResponseEntity<>(qcService.getDetailByGrnId(id),
                HttpStatus.OK);
    }
}
