package com.agi.aesl.erpscm.user_application_validation.dto.request;

import com.agi.aesl.erpscm.comment.entity.CommentAttachment;
import com.agi.aesl.erpscm.comment.enums.ActionType;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import lombok.Data;

import java.util.List;

@Data
public class RejectDto {
    RefDto verifier;
    private DomainType domainType;
    private Long domainId;
    private String comment;
    private ActionType actionType;
    List<CommentAttachment> attachments;

}
