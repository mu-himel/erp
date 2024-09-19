package com.agi.aesl.erpscm.cs.dto;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.cs.entity.CsDeliveryDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CsVendorDetailDto {
    private Long id;
    private Long vendorId;
    private String transactionType;
    private BigDecimal orderQty;
    private BigDecimal vatAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private ReferenceObjectDto priceQuotation;
    private List<CsDeliveryDetailDto> warehouses;
}
