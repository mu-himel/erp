package com.agi.aesl.erpscm.comment.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
public class CommendServiceImpl implements CommentService{

    @Autowired  
    private CommentRepository commentRepository;

    @Autowired
    private CommentAttachmentRepository commentAttachmentRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Override
    public void addComment(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setCommentedBy(new Employee(commentDto.getCommentedBy().getId()));
        comment.setMessage(commentDto.getMessage());
        comment.setDomainId(commentDto.getDomainId());
        comment.setDomainType(commentDto.getDomainType());
        comment.setAttachments(commentDto.getAttachments().stream().map(attachment->{
            attachment.setComment(comment);
            return attachment;
        }).collect(Collectors.toList()));
        commentRepository.save(comment);
        
    }

    @Override
    public void addComment(Comment comment) {
        comment.setAttachments(comment.getAttachments().stream().map(attachment->{
            attachment.setComment(comment);
            return attachment;
        }).collect(Collectors.toList()));
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
    public void addComment(List<Comment> comments) {
        commentRepository.saveAll(comments);
    }

    @Override
    public List<?> getCommentsByDomain(DomainType domainType, Long domainId) {
        return commentRepository.findAllByDomainTypeAndDomainId(domainType,domainId);
    }

    @Override
    public ByteArrayResource load(String domainType, Long domainId, Long id, String filename) {
        Optional<CommentAttachment> CommentAttachmentOp = commentAttachmentRepository.findByCommentIdAndAttachmentPath(id,filename);
        if(CommentAttachmentOp.isEmpty()){
            return null;
        }
        try {
            CommentAttachment commentAttachment = CommentAttachmentOp.get();
            Path path = Path.of("./uploads/"+domainType+"/"+domainId+"/comments/"+commentAttachment.getAttachmentPath());
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public List<FileUploadResponse> uploadAttachment(String domainType, Long domainId, List<MultipartFile> attachments) {
        Path path = Path.of("./uploads/"+domainType+"/"+domainId+"/comments/");
        List<FileUploadResponse> fileUploadResponses = new ArrayList<>(); 
        for(MultipartFile attachment : attachments){
         fileUploadResponses.add(fileUploadService.uploadFile(path, attachment));
        }
        return fileUploadResponses;
    }

    

    
    
}
