package com.agi.aesl.erpscm.modules.dto;

import java.util.ArrayList;
import java.util.List;


import lombok.Data;

@Data
public class VerifierConfig {
    private Long parentDepartment;
    private String reportingManager;
    private Boolean verificationRequired;
    private String employeeDepartment;
    private List<VerifierInfo> verifiers=new ArrayList<>();
    private Long level;
}
