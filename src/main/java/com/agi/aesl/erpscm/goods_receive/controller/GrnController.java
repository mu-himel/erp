package com.agi.aesl.erpscm.goods_receive.controller;

import com.agi.aesl.erpscm.common.BaseController;

import com.agi.aesl.erpscm.goods_receive.dto.request.GrnManualItemDetailDto;
import com.agi.aesl.erpscm.goods_receive.dto.request.GrnManualRequestDto;

import com.agi.aesl.erpscm.goods_receive.enums.GrnMode;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/goods-receive-note")
@RequiredArgsConstructor
public class GrnController extends BaseController {


    private final GrnService grnService;

    @PostMapping
    public ResponseEntity<Void> createGrn(
            @AuthenticationPrincipal Jwt token,
            @RequestBody @Valid GrnManualRequestDto goodReceiveNoteDto
            ){
        grnService.createAutoGrn(token,goodReceiveNoteDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @PostMapping("/manual")
    public ResponseEntity<Object> createManualGrn(
            @AuthenticationPrincipal Jwt token,
            @RequestBody @Valid GrnManualRequestDto grnManualDto
    ){
        Map<String,Object> result = new HashMap<>();
        result.put("grnNo",grnService.createManualGrn(token, grnManualDto, GrnMode.MANUAL));
        return new ResponseEntity<>(result,HttpStatus.CREATED);
    }



    @GetMapping("/all")
    public ResponseEntity<Object> getAllGrn(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("grnNo") Optional<String> grnNo,
            @RequestParam("itemQty") Optional<Integer> qty,
            @RequestParam("receivedQty") Optional<Integer> receivedQty,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate,
            @RequestParam("grnStatus") Optional<String> grnStatus,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                grnService.getAllGrn(token,page,size,grnNo,qty, receivedQty,fromDate,toDate, grnStatus),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getGrnById(
            @PathVariable("id") Long id
    ){
        return new ResponseEntity<>(grnService.getGrnById(id,true),HttpStatus.OK);
    }

    @PutMapping("/{id}/receive-po")
    public ResponseEntity<Void> receiveGrn(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody List<GrnManualItemDetailDto> grnManualRequestDto
            ){
        grnService.receivedPO(token,id, grnManualRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/decline-po")
    public ResponseEntity<Void> declineGrn(@PathVariable("id") Long id,
                                        @AuthenticationPrincipal Jwt token,
                                        @RequestBody NoteDto noteDto
    ){
        grnService.declinePO(token, id,noteDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
