package com.agi.aesl.erpscm.user_application_validation.dto.response;

import lombok.Data;

/**
 * ApprovalPanel
 */
@Data
public class ApprovalPanel {

    private Long id;
    private String name;
    private Long departmentId;
    private Long designationId;
    private String userId;
    private String departmentName;
    private String designationName;
    private Integer departmentLevel;
}