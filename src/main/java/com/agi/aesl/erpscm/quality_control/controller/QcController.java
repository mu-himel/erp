package com.agi.aesl.erpscm.quality_control.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import com.agi.aesl.erpscm.quality_control.dto.request.QcDto;
import com.agi.aesl.erpscm.quality_control.service.QcService;
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
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size){
        return new ResponseEntity<>(
                grnService.getAllGrnPendingQC(page,size, fromDate, toDate),
                HttpStatus.OK
        );
    }

    @PostMapping
    public ResponseEntity<?> addQc(
            @AuthenticationPrincipal Jwt token,
            @RequestBody QcDto qualityControlDto) throws IllegalAccessException {
        qcService.addQc(token,qualityControlDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> rejectQc(
            @PathVariable("id") Long id
    ){
        qcService.rejectQc(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
