package com.agi.aesl.erpscm.comment.dto;

import java.util.List;

import com.agi.aesl.erpscm.comment.entity.CommentAttachment;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import lombok.Data;

@Data
public class CommentDto {

    private Long id;
    private Long domainId;
    private DomainType domainType;
    private CommentedByDto commentedBy;
    private List<CommentAttachment> attachments;
    private String message;
}
