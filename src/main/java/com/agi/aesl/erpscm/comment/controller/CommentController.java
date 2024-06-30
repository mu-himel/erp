package com.agi.aesl.erpscm.comment.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.agi.aesl.erpscm.comment.dto.CommentDto;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.service.DemandService;
import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import com.agi.aesl.erpscm.user_application_validation.dto.request.ApproveDto;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController extends BaseController{
    @Autowired
    private CommentService commentService;

    @Autowired
    private UserApplicationValidatorService<?> verificationService;

    @Autowired
    private DemandService demandService;
    
 
    @PostMapping("/approve")
    public ResponseEntity<?> approve(@RequestBody ApproveDto approveDto){
        if(approveDto.getDomainType().equals(DomainType.DEMAND)){
            verificationService.setVerificationDomainService(demandService);
            verificationService.approve(approveDto);
        }
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    
        @PostMapping
        public ResponseEntity<?> addComment(@RequestBody CommentDto commentDto){
            commentService.addComment(commentDto);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        
    
        @PostMapping("/{domainType}/{domainId}")
        public ResponseEntity<?> uploadAttachment(
            @PathVariable("domainType") String domainType,
            @PathVariable("domainId") Long domainId,
            @RequestPart("files") List<MultipartFile> files){
              List<FileUploadResponse> response =  commentService.uploadAttachment(domainType,domainId,files);
            return new ResponseEntity<>(response,HttpStatus.CREATED);
        }
    
        @GetMapping(value = "/attachments/{id}/{domainId}/{domainType}/{filename:.+}",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public ResponseEntity<?> getImage(
                @PathVariable("id") Long id,
                @PathVariable("domainId") Long domainId,
                @PathVariable("domainType") String domainType,
                @PathVariable String filename) {
    
                String mediaType=null;
            
                if(filename.contains("jpg")){
                    mediaType = "image/jpg";
                }
                if(filename.contains(".pdf")){
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
