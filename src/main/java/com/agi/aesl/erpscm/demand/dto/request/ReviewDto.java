package com.agi.aesl.erpscm.demand.dto.request;

import java.util.List;

import com.agi.aesl.erpscm.comment.entity.CommentAttachment;
import com.agi.aesl.erpscm.comment.enums.DomainType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
   
    DomainType domainType;
    String message;
    List<CommentAttachment> attachments; 
}
