package com.agi.aesl.erpscm.organization.dto.request;

import lombok.Data;

@Data
public class OrgRequestDto {
    private String name;
    private String code;
    private String serviceIpAddress;
    private String serviceUsername;
    private String servicePassword;
}
