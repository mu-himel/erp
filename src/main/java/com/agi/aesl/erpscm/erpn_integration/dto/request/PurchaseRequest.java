package com.agi.aesl.erpscm.erpn_integration.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseRequest {
    private String srnNo;
    private String vendorCpsId;
    private String vatType;
    private String invoice;
    List<PurchaseRequestItem> itemList;
}
