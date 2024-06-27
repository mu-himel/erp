package com.agi.aesl.erpscm.comment.service;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import com.agi.aesl.erpscm.comment.dto.CommentDto;
import com.agi.aesl.erpscm.comment.entity.Comment;
import com.agi.aesl.erpscm.comment.entity.CommentAttachment;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;

public interface CommentService {
    void addComment(CommentDto comment);
    void addComment(Comment comment);
    void addComment(List<Comment> comments);

    Comment prepareComment(Employee employee, DomainType domainType, Long domainId,String msg,
        List<CommentAttachment> attachments);

    List<?> getCommentsByDomain(DomainType domainType, Long domainId);
    List<FileUploadResponse> uploadAttachment(String domainType, Long domainId, List<MultipartFile> attachment);

    ByteArrayResource load(String domainType, Long domainId, Long id, String filename);
}
