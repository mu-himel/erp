package com.agi.aesl.erpscm.comment.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.agi.aesl.erpscm.comment.dto.CommentDto;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.service.DemandService;
import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import com.agi.aesl.erpscm.user_application_validation.dto.request.ApproveDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.VerifyDto;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController extends BaseController{

    private final CommentService commentService;


    private final UserApplicationValidatorService<?> verificationService;


    private final DemandService demandService;
    
 
    @PostMapping("/approve")
    public ResponseEntity<Void> approve(
            @AuthenticationPrincipal Jwt token,
            @RequestBody ApproveDto approveDto){
        if(approveDto.getDomainType().equals(DomainType.DEMAND)){
            verificationService.setVerificationDomainService(demandService);
            verificationService.approve(token, approveDto);
        }
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/verify")
    public ResponseEntity<Void> verify(
            @AuthenticationPrincipal Jwt token,
            @RequestBody VerifyDto verifyDto){
        if(verifyDto.getDomainType().equals(DomainType.DEMAND)){
            verificationService.setVerificationDomainService(demandService);
            verificationService.verify(token, verifyDto);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/review")
    public ResponseEntity<Void> review(@RequestBody VerifyDto verifyDto){
        if(verifyDto.getDomainType().equals(DomainType.DEMAND)){
            verificationService.setVerificationDomainService(demandService);
            verificationService.review(verifyDto);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

        @PostMapping
        public ResponseEntity<Void> addComment(@RequestBody CommentDto commentDto){
            commentService.addComment(commentDto);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        @PostMapping("/{domainType}/{domainId}")
        public ResponseEntity<Object> uploadAttachment(
            @PathVariable("domainType") String domainType,
            @PathVariable("domainId") Long domainId,
            @RequestPart("files") List<MultipartFile> files){
              List<FileUploadResponse> response =  commentService.uploadAttachment(domainType,domainId,files);
            return new ResponseEntity<>(response,HttpStatus.CREATED);
        }

        @GetMapping(value = "/attachments/{id}/{domainId}/{domainType}",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public ResponseEntity<Object> getImage(
                @PathVariable("id") Long id,
                @PathVariable("domainId") Long domainId,
                @PathVariable("domainType") String domainType,
                @RequestParam("filename") String filename) {
    
                String mediaType=null;


                    if (filename.contains("jpg")) {
                        mediaType = "image/jpg";
                    }
                    if (filename.contains(".pdf")) {
                        mediaType = "application/pdf";
                    }

            
                return ResponseEntity
                    .ok()
                    .header("Content-Type", mediaType)
                    .body(
                        commentService.load(domainType,domainId,id,filename)
                    );
    
        }
}
