package com.agi.aesl.erpscm.purchase_order.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseRequestDto {

    Long csId;
    private String vendorName;
    private Long vendorId;
    private String vendorEmail;
    private String phoneNo;
    private String deliveryChargeType;
    List<PoTermsCondition> termsConditions;
    List<PoDetailReqDto> details;
}
