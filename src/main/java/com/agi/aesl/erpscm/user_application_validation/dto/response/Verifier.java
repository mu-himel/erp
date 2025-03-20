package com.agi.aesl.erpscm.user_application_validation.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Verifier {
    private String id;
    private String name;
    private Boolean verified;
    private Long departmentId;
    private Long parentDepartmentId;
    String departmentName;
    Integer departmentLevel;

    Long designationId;

    String designation;



}
