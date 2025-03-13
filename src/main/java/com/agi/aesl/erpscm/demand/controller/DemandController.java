package com.agi.aesl.erpscm.demand.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.DemandReceiveDto;
import com.agi.aesl.erpscm.demand.dto.request.DemandRequestDto;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.demand.service.DemandService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/demands")
@RequiredArgsConstructor
public class DemandController extends BaseController{
    

    private final DemandService demandService;

    @PostMapping
    public ResponseEntity<Void> createDemand(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody @Valid DemandRequestDto demandRequestDto){
        demandService.createDemand(token, uri, demandRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDemand(
        @AuthenticationPrincipal Jwt token,
        @PathVariable("id") Long id,
        @RequestHeader("uri") String uri,
    @RequestBody @Valid DemandRequestDto demandRequestDto){
        demandService.updateDemand(token, uri,id, demandRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelDemand(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") Long id,
            @RequestHeader("uri") String uri,
            @RequestParam("categories") String categories,
            @RequestBody NoteDto noteDto
            ){
        demandService.cancelDemand(token,id,uri,categories, noteDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/my")
    public ResponseEntity<Object> getMyDemands(@AuthenticationPrincipal Jwt loggedInUser,
                                          @RequestParam("page") Optional<Integer> page,
                                          @RequestParam("size") Optional<Integer> size,
                                          @RequestParam("demandNo") Optional<String> demandNo,
                                          @RequestParam("categoryId") Optional<Long> categoryId,
                                          @RequestParam("fromDate") Optional<String> fromDate,
                                          @RequestParam("toDate") Optional<String> toDate
                                          ){
        return new ResponseEntity<>(
                demandService.getMyDemands(loggedInUser,page,size,demandNo,categoryId, fromDate, toDate),
            HttpStatus.OK
        );
    }

    @GetMapping("/stock-by/sub-category")
    public ResponseEntity<Object> getStockBySubCategory(
            @RequestParam("subCategoryId") Long subCategoryId,
            @RequestParam("warehouseId") Long warehouseId
    ){
        return new ResponseEntity<>(
                demandService.getStockBySubCategory(subCategoryId,warehouseId),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getDemandDetail(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                demandService.getDemandDetail(id),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<Object> getPendingDemands(
                    @AuthenticationPrincipal Jwt loggedInUser,
                    @RequestParam("page") Optional<Integer> page,
                    @RequestParam("size") Optional<Integer> size,
                    @RequestParam("demandNo") Optional<String> demandNo,
                    @RequestParam("categoryId") Optional<Long> categoryId,
                    @RequestParam("fromDate") Optional<String> fromDate,
                    @RequestParam("toDate") Optional<String> toDate,
                    @RequestParam("daysRemain") Optional<Integer> daysRemain

                    ){

        return new ResponseEntity<>(
                demandService.getAllDemands(loggedInUser, page,size,
                        demandNo,categoryId,
                        fromDate,toDate, daysRemain
                        ),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<Object> getPendingVerificationDemands(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("demandNo") Optional<String> demandNo,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate

            ){

                return new ResponseEntity<>(
                        demandService.getAllPendingVerificationDemands(loggedInUser, page,size,
                                demandNo,categoryId,
                                fromDate,toDate),
                        HttpStatus.OK
                );
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<Object> getPendingApprovalDemands(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("demandNo") Optional<String> demandNo,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate

    ){

        return new ResponseEntity<>(
                demandService.getAllPendingApprovalDemands(loggedInUser, page,size,
                        demandNo,categoryId,
                        fromDate,toDate ),
                HttpStatus.OK
        );
    }

    @GetMapping("/close")
    public ResponseEntity<Object> getCloseDemands(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("demandNo") Optional<String> demandNo,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate
            ){

        return new ResponseEntity<>(
                demandService.getAllCloseDemands(loggedInUser, page,size,
                        demandNo,categoryId,fromDate, toDate),
                HttpStatus.OK
        );
    }

    @PostMapping("/receive")
    public ResponseEntity<Void> demandReceive(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.receiveDemandItem(loggedInUser, demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PostMapping("/sent")
    public ResponseEntity<Void> demandSent(
           @AuthenticationPrincipal Jwt token,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.sentDemandItem(token, demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PostMapping("/decline")
    public ResponseEntity<Void> declineDemand(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.declineDemandItem(loggedInUser,demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PutMapping("/reject")
    public ResponseEntity<Void> rejectDemand(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.rejectDemandItem(loggedInUser,demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PutMapping("/close-by-store")
    public ResponseEntity<Void> closeDemand(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.closeDemandItem(loggedInUser,demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PutMapping("/reject-by-panel")
    public ResponseEntity<Void> rejectDemandPanel(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.rejectDemand(loggedInUser,demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PutMapping("/resent")
    public ResponseEntity<Void> resentDemand(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.resendDemandItem(loggedInUser,demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PostMapping("/review/{id}")
    public ResponseEntity<Void> review(
            @AuthenticationPrincipal Jwt loggedInUser,
            @PathVariable("id") Long id,
            @RequestBody ReviewDto reviewDto
    ){
        demandService.reviewDemand(loggedInUser,id,reviewDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @GetMapping("/next-id")
    public ResponseEntity<Object> getNextId(){
        Map<String,Object> response = new HashMap<>();
        response.put("code",demandService.getNextDemandNo());
        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );
    }

}
