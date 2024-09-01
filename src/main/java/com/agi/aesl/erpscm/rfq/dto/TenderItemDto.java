package com.agi.aesl.erpscm.rfq.dto;

import lombok.Data;

import java.util.List;

@Data
public class TenderItemDto {
    private String productDescription;
    private String brandName;
    private String specification;
    private List<TenderItemDeliveryDetail> deliveryDetails;
    private Long orderQuantity;
}
