package com.agi.aesl.erpscm.internal_requisition.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.CreateIRDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.UpdateIRDetailDto;
import com.agi.aesl.erpscm.internal_requisition.repository.InternalRequisitionRepository;
import com.agi.aesl.erpscm.internal_requisition.service.IrService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/internal-requisitions")
public class InternalReqController extends BaseController {
    @Autowired
    private IrService internalRequisitionService;

    @PostMapping
    public ResponseEntity<?> createIR(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody CreateIRDto createIrDto){
        internalRequisitionService.createInternalRequisition(token,uri,createIrDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> getAllIR(
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate
    ){
        return new ResponseEntity<>(
                internalRequisitionService.getAllInternalRequisitions(page,size,fromDate,toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<?> getPendingVerificationIR(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate
    ){
        return new ResponseEntity<>(
                internalRequisitionService.getAllPendingVerificationIrs(token, page,size, fromDate,toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<?> getPendingApprovalIR(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate
    ){
        return new ResponseEntity<>(
                internalRequisitionService.getAllPendingApprovalIrs(token,page, size,fromDate,toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/closed")
    public ResponseEntity<?> getClosedIR(
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate
    ){

        return new ResponseEntity<>(
                internalRequisitionService.getAllClosedIr(page,size,fromDate,toDate),
                HttpStatus.OK
        );

    }

    @GetMapping("/pending-requisitions")
    public ResponseEntity<?> getPendingRequisitionForController(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate
    ){
        return new ResponseEntity<>(
                internalRequisitionService.getAllVerifiedOrApprovedIrs(token,page, size,fromDate,toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/processing-requisitions")
    public ResponseEntity<?> getProcessingIrsForController(
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate
    ){
        return new ResponseEntity<>(
                internalRequisitionService.getAllProcessedIrs(page,size,fromDate,toDate),
                HttpStatus.OK
        );
    }

    @GetMapping("/next-ir-no")
    public ResponseEntity<?> getNextIrNo(){
        Map<String,Object> response = new HashMap<>();
        response.put("code",internalRequisitionService.getNextIrNo());
        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                internalRequisitionService.getDetail(id, InternalRequisitionRepository.IrDetail.class),
                HttpStatus.OK
        );
    }


    @PutMapping("/review/{id}")
    public ResponseEntity<?> reviewIr(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
    ){
        internalRequisitionService.reviewIr(token,id,reviewDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectIr(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestBody NoteDto noteDto
    ){
        internalRequisitionService.rejectIr(token,id,noteDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateWarehouseRequirement(
            @AuthenticationPrincipal Jwt token,
            @RequestBody UpdateIRDetailDto updateIrDto
    ){
        internalRequisitionService.updateIR(token,updateIrDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/stats-by-item-attribute/{id}")
    public ResponseEntity<?> getWarehouseListByItemId(@PathVariable("id") Long id){
        return new ResponseEntity<>(internalRequisitionService.getWarehouses(id), HttpStatus.OK);
    }
}
