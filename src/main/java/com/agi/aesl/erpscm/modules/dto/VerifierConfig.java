package com.agi.aesl.erpscm.modules.dto;

import lombok.Data;

@Data
public class VerifierConfig {
    private Long id;
    private String criteriaGroup;
    private String criteriaColumn;
    private String criteriaIdValue;
    private String criteriaText;
    private Long level;
}
