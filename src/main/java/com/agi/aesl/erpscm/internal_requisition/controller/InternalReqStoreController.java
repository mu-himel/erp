package com.agi.aesl.erpscm.internal_requisition.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.internal_requisition.dto.request.ReceiveStockDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.StoreIRReqDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.TransferStockDto;
import com.agi.aesl.erpscm.internal_requisition.service.IrStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/ir-store")
@RequiredArgsConstructor
public class InternalReqStoreController extends BaseController {


    private final IrStoreService irStoreService;

    @PostMapping
    public ResponseEntity<Void> submit(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody StoreIRReqDto storeIRReqDto
    ){
        irStoreService.submit(token, uri, storeIRReqDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/pending")
    public ResponseEntity<Object> pendingStoreIR(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate
            ){
        return new ResponseEntity<>(irStoreService.getPendingStoreIrs(token,page,size,fromDate,toDate), HttpStatus.OK);
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<Object> pendingStoreIrVerification(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(irStoreService.getPendingStoreIrVerification(token, page, size), HttpStatus.OK);
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<Object> pendingStoreIrApproval(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(irStoreService.getPendingStoreIrApproval(token, page, size), HttpStatus.OK);
    }

    @GetMapping("/receive-requisitions")
    public ResponseEntity<Object> receiveRequisition(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(irStoreService.getReceiveStoreRequisitions(token, page, size),
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getStoreIrDetail(
            @PathVariable("id") Long id
    ){
        return new ResponseEntity<>(irStoreService.getStoreIRDetail(id), HttpStatus.OK);
    }

    @GetMapping("/ready-for-transfer")
    public ResponseEntity<Object> readyForTransfer(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(irStoreService.getReadyForTransfer(token, page, size), HttpStatus.OK);
    }

    @PutMapping("/transfer")
    public ResponseEntity<Void> updateTransferStock(
            @RequestBody TransferStockDto transferStockDto
    ){
        irStoreService.transferStock(transferStockDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/receive")
    public ResponseEntity<Void> receiveNote(@RequestBody ReceiveStockDto receiveStockDto){
        irStoreService.receiveStock(receiveStockDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/decline")
    public ResponseEntity<Void> declineByStore(@RequestBody ReceiveStockDto receiveStockDto){
        irStoreService.declineStock(receiveStockDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/accept-return")
    public ResponseEntity<Void> acceptReturn(@RequestBody ReceiveStockDto receiveStockDto){
        irStoreService.acceptReturn(receiveStockDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
