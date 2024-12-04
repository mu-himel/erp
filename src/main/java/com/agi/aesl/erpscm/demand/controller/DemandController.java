package com.agi.aesl.erpscm.demand.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.dto.request.DemandReceiveDto;
import com.agi.aesl.erpscm.demand.dto.request.DemandRequestDto;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.demand.service.DemandService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/demands")
public class DemandController extends BaseController{
    
    @Autowired
    private DemandService demandService;

    @PostMapping
    public ResponseEntity<?> createDemand(
            @AuthenticationPrincipal Jwt token,
            @RequestHeader("uri") String uri,
            @RequestBody @Valid DemandRequestDto demandRequestDto){
        demandService.createDemand(token, uri, demandRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDemand(
        @AuthenticationPrincipal Jwt token,
        @PathVariable("id") Long id,
        @RequestHeader("uri") String uri,
    @RequestBody @Valid DemandRequestDto demandRequestDto){
        demandService.updateDemand(token, uri,id, demandRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelDemand(
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
    public ResponseEntity<?> getMyDemands(@AuthenticationPrincipal Jwt loggedInUser,
                                          @RequestParam("page") Optional<Integer> page,
                                          @RequestParam("size") Optional<Integer> size,
                                          @RequestParam("fromDate") Optional<String> fromDate,
                                          @RequestParam("toDate") Optional<String> toDate
                                          ){
        return new ResponseEntity<>(
                demandService.getMyDemands(loggedInUser,page,size, fromDate, toDate),
            HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDemandDetail(@PathVariable("id") Long id){
        return new ResponseEntity<>(
                demandService.getDemandDetail(id),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<?> getPendingDemands(
                    @AuthenticationPrincipal Jwt loggedInUser,
                    @RequestParam("page") Optional<Integer> page,
                    @RequestParam("size") Optional<Integer> size,
                    @RequestParam("demandNo") Optional<String> demandNo,
                    @RequestParam("fromDate") Optional<String> fromDate,
                    @RequestParam("toDate") Optional<String> toDate,
                    @RequestParam("daysRemain") Optional<Integer> daysRemain

                    ){

        return new ResponseEntity<>(
                demandService.getAllDemands(loggedInUser, page,size,
                        demandNo,
                        fromDate,toDate, daysRemain
                        ),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<?> getPendingVerificationDemands(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate

            ){

                return new ResponseEntity<>(
                        demandService.getAllPendingVerificationDemands(loggedInUser, page,size,fromDate,toDate),
                        HttpStatus.OK
                );
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<?> getPendingApprovalDemands(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate

    ){

        return new ResponseEntity<>(
                demandService.getAllPendingApprovalDemands(loggedInUser, page,size,
                        fromDate,toDate ),
                HttpStatus.OK
        );
    }

    @GetMapping("/close")
    public ResponseEntity<?> getCloseDemands(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("fromDate") Optional<String> fromDate,
            @RequestParam("toDate") Optional<String> toDate
            ){

        return new ResponseEntity<>(
                demandService.getAllCloseDemands(loggedInUser, page,size,fromDate, toDate),
                HttpStatus.OK
        );
    }

    @PostMapping("/receive")
    public ResponseEntity<?> demandReceive(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.receiveDemandItem(loggedInUser, demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PostMapping("/sent")
    public ResponseEntity<?> demandSent(
           @AuthenticationPrincipal Jwt token,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.sentDemandItem(token, demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PostMapping("/decline")
    public ResponseEntity<?> declineDemand(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.declineDemandItem(loggedInUser,demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PutMapping("/reject")
    public ResponseEntity<?> rejectDemand(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.rejectDemandItem(loggedInUser,demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PutMapping("/close-by-store")
    public ResponseEntity<?> closeDemand(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.closeDemandItem(loggedInUser,demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PutMapping("/reject-by-panel")
    public ResponseEntity<?> rejectDemandPanel(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.rejectDemand(loggedInUser,demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PutMapping("/resent")
    public ResponseEntity<?> resentDemand(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody DemandReceiveDto demandReceiveDto
    ){
        demandService.resendDemandItem(loggedInUser,demandReceiveDto);
        return new ResponseEntity<>(
                HttpStatus.NO_CONTENT
        );
    }

    @PostMapping("/review/{id}")
    public ResponseEntity<?> review(
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
    public ResponseEntity<?> getNextId(){
        Map<String,Object> response = new HashMap<>();
        response.put("code",demandService.getNextDemandNo());
        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );
    }

}
