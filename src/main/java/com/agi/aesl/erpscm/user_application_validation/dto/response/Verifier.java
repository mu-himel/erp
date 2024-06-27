package com.agi.aesl.erpscm.user_application_validation.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Verifier {
    private Long id;
    private String name;
    private Boolean verified;
    private Long departmentId;
    private Long parentDepartmentId;
    String departmentName;
    Integer departmentLevel;

    Long designationId;

    String designation;

    public Verifier(Long id, String name, Boolean verified,
                    Long departmentId,  Long parentDepartmentId,
                    String departmentName, Integer departmentLevel,
                    Long designationId, String designation) {

        this.id = id;
        this.name = name;
        this.verified = verified;
        this.departmentId = departmentId;
        this.parentDepartmentId = parentDepartmentId;
        this.departmentName = departmentName;
        this.departmentLevel = departmentLevel;
        this.designationId = designationId;
        this.designation = designation;
    }

}
