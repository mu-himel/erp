package com.agi.aesl.erpscm.user_application_validation.dto.response;

import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppliedVADto {
    private List<VerifierInfo> verifiers;
    private List<ApprovalPanel> panels;
}
