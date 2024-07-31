package com.agi.aesl.erpscm.erpn_integration.dto.response;

import lombok.Data;

@Data
public class VendorListInfo {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Integer score;
    private String verificationStatus;
}
