package com.agi.aesl.erpscm.quality_control.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.quality_control.dto.request.QcDto;
import com.agi.aesl.erpscm.quality_control.service.QcService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/qc")
public class QcController extends BaseController {

    @Autowired
    private QcService qcService;

    @Autowired
    private GrnService grnService;

    @GetMapping
    public ResponseEntity<?> getAllForQc(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("grnNo") Optional<String> grnNo,
            @RequestParam("items") Optional<Integer> qty,
            @RequestParam("receivedQty") Optional<Integer> receivedQty,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(
                grnService.getAllGrnPendingQC(token, page,
                        size, grnNo, qty,
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

    @GetMapping("/closed")
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
        qcService.addQc(token, uri, qualityControlDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> rejectQc(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody NoteDto noteDto
            ){
        qcService.rejectQc(token,id, noteDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQcDetail(@PathVariable("id") Long id){
        return new ResponseEntity<>(qcService.getDetailByGrnId(id),
                HttpStatus.OK);
    }
}
