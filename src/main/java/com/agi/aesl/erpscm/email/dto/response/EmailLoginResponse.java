package com.agi.aesl.erpscm.email.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailLoginResponse {
    Long timestamp;
    Integer statusCode;
    String status;
    String message;
    Map<String,Object> content;
}
