package com.agi.aesl.erpscm.comment.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.comment.dto.CommentInfo;
import com.agi.aesl.erpscm.comment.enums.ActionType;
import com.agi.aesl.erpscm.exception.AesException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.agi.aesl.erpscm.comment.dto.CommentDto;
import com.agi.aesl.erpscm.comment.entity.Comment;
import com.agi.aesl.erpscm.comment.entity.CommentAttachment;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.repository.CommentAttachmentRepository;
import com.agi.aesl.erpscm.comment.repository.CommentRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import com.agi.aesl.erpscm.fileupload.service.FileUploadService;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{


    private final CommentRepository commentRepository;


    private final CommentAttachmentRepository commentAttachmentRepository;


    private final FileUploadService fileUploadService;

    @Value("${upload.dir}")
    private String uploadDir;

    private static final String PATH_SEPARATOR="/";

    @Override
    public void addComment(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setCommentedBy(new Employee(commentDto.getCommentedBy().getId()));
        comment.setMessage(commentDto.getMessage());
        comment.setDomainId(commentDto.getDomainId());
        comment.setDomainType(commentDto.getDomainType());
        comment.setActionType(commentDto.getActionType());
        comment.setAttachments(commentDto.getAttachments().stream().map(attachment->{
            attachment.setComment(comment);
            return attachment;
        }).toList());
        commentRepository.save(comment);
        
    }

    @Override
    public void addComment(Comment comment) {
        comment.setAttachments(comment.getAttachments().stream().map(attachment->{
            attachment.setComment(comment);
            return attachment;
        }).toList());
        commentRepository.save(comment);
        
    }

    @Override
    public Comment prepareComment(Employee employee, DomainType domainType, Long domainId, String msg,
            List<CommentAttachment> attachments) {
                
        Comment comment = new Comment();
        comment.setCommentedBy(employee);
        comment.setMessage(msg);
        comment.setDomainType(domainType);
        comment.setDomainId(domainId);
        comment.setAttachments(attachments);
        return comment;
    }

    @Override
    public Comment prepareComment(Employee employee, DomainType domainType, ActionType actionType, Long domainId, String msg, List<CommentAttachment> attachments) {
        Comment comment = new Comment();
        comment.setCommentedBy(employee);
        comment.setMessage(msg);
        comment.setDomainType(domainType);
        comment.setActionType(actionType);
        comment.setDomainId(domainId);
        comment.setAttachments(attachments);
        return comment;
    }

    @Override
    public void addComment(List<Comment> comments) {
        commentRepository.saveAll(comments);
    }

    @Override
    public List<CommentInfo> getCommentsByDomain(DomainType domainType, Long domainId) {
        return commentRepository.findAllByDomainTypeAndDomainId(domainType,domainId);
    }

    @Override
    public ByteArrayResource load(String domainType, Long domainId, Long id, String filename) {
        Optional<CommentAttachment> commentAttachmentOp = commentAttachmentRepository.findById(id);
        if(commentAttachmentOp.isEmpty()){
            return null;
        }
        try {
            CommentAttachment commentAttachment = commentAttachmentOp.get();
            if(!commentAttachment.getAttachmentPath().contains(filename)){
                return null;
            }
            Path path = Path.of("."+commentAttachment.getAttachmentPath());
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new AesException("Could not read the file!");
            }
        } catch (IOException e) {
            throw new AesException("Error: " + e.getMessage());
        }
    }

    @Override
    public List<FileUploadResponse> uploadAttachment(String domainType, Long domainId, List<MultipartFile> attachments) {
        Path path = Path.of(uploadDir+domainType+PATH_SEPARATOR+domainId+"/comments/");
        List<FileUploadResponse> fileUploadResponses = new ArrayList<>(); 
        for(MultipartFile attachment : attachments){
         fileUploadResponses.add(fileUploadService.uploadFile(path, attachment));
        }
        return fileUploadResponses;
    }

    

    
    
}
