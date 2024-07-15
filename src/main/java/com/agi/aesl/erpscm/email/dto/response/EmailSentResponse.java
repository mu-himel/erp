package com.agi.aesl.erpscm.email.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailSentResponse {
    private Boolean error;
    private Boolean success;
    private String status;
    private String errorMessage;
}
