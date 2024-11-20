package com.agi.aesl.erpscm.price_quotation.dto.request;

import com.agi.aesl.erpscm.common.enums.DeliveryCharge;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceQuotationSummaryDto {
    private DeliveryCharge deliveryCharge;
    private BigDecimal deliveryChargeAmount;
    private Boolean mushak;
    private Boolean isVatAdded;
    private String vatPercent;
    private String aitPercent;
    private String vatAmount;
    private String aitAmount;
    private Boolean isAitAdded;
    private BigDecimal subTotalPrice;
    private BigDecimal totalPrice;
    private Long creditPaymentDuration;
    private String creditPaymentUnit;
    private String note;
}
