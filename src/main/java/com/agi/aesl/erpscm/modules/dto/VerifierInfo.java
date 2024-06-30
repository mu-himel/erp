package com.agi.aesl.erpscm.modules.dto;

import lombok.Data;

/**
 * VerifierInfo
 */
@Data
public class VerifierInfo {

    String id;
    String name;
    Boolean verified;
    Long departmentId;
    Long parentDepartmentId;
    String departmentName;
    Integer departmentLevel;

    Long designationId;

    String designation;
}