package com.agi.aesl.erpscm.indent.dto.request;

import com.agi.aesl.erpscm.comment.entity.CommentAttachment;
import lombok.Data;

import java.util.List;

@Data
public class IndentRejectDto {

    private Long id;
    private String note;
    private List<CommentAttachment> attachments;

}
