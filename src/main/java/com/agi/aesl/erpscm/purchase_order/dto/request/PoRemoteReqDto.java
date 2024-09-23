package com.agi.aesl.erpscm.purchase_order.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PoRemoteReqDto {
    private Long id;
    private String poNo;
    private Long vendorId;
    private String tenderNo;
    private Long poDate;
    private String categoryCode;
    private Long deliveryDate;
    private Long offerId;
    private List<PoRemoteDetailReqDto> orderDetails;
}
