package com.agi.aesl.erpscm.price_quotation.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PriceQuotationDetailReqDto {
    private String itemAttributeName;
    private String extendedAttributes;
    private String brandName;
    private BigDecimal rfqQty;
    private BigDecimal unitPrice;
    private Integer estDeliveryDays;
    private Integer warrantyDuration;
    private String warrantyUnit;
    private List<PriceQuotationDeliveryDetailDto> deliveryDetails;
}
