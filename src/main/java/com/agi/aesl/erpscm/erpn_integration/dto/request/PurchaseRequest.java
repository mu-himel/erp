package com.agi.aesl.erpscm.erpn_integration.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseRequest {
    // srn no
    // vendorId
    private String supplierName;
    private String costCenter;
    List<PurchaseRequestItem> items;
}
