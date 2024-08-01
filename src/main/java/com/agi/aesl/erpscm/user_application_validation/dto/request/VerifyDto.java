package com.agi.aesl.erpscm.user_application_validation.dto.request;

import java.util.List;

import com.agi.aesl.erpscm.comment.entity.CommentAttachment;
import com.agi.aesl.erpscm.comment.enums.ActionType;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyDto {
    RefDto verifier;
    Long domainId;
    DomainType domainType;
    String comment;
    ActionType actionType;
    RefDto reviewer;
    List<CommentAttachment> attachments;
}
