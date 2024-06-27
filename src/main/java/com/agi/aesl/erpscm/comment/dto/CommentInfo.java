package com.agi.aesl.erpscm.comment.dto;

import java.util.List;

import com.agi.aesl.erpscm.comment.entity.CommentAttachment;
import com.agi.aesl.erpscm.employee.entity.Employee;

/**
 * CommentInfo
 */
public interface CommentInfo {
    Long getId();
    Employee getCommentedBy();
    String getMessage();
    List<CommentAttachment> getAttachments();
    
}