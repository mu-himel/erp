package com.agi.aesl.erpscm.email.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EmailSentRequestDto {
    private String subject;
    private String content;
    private Boolean isHtml=true;
    private List<String> to = new ArrayList<>();
}
