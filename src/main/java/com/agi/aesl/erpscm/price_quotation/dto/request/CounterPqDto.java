package com.agi.aesl.erpscm.price_quotation.dto.request;

import com.agi.aesl.erpscm.price_quotation.enums.CreditType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CounterPqDto {
    private List<CounterItemDto> offerItems;
    private List<DeliveryDetailDto> warehouses;
    private List<CounterTermAndConditionDto> termsAndConditions;
    private CreditType creditType;
    private boolean mushakIncluded;
    private BigDecimal totalDeliveryChargeAmount;
    // private boolean deliveryChargeIncluded;
    private boolean vatIncluded;
    private boolean aitIncluded;
    private BigDecimal vatAmount;
    private BigDecimal aitAmount;
    private BigDecimal vatPercent;
    private BigDecimal aitPercent;
    private String note;
    private BigDecimal finalOfferPrice;
    private Boolean isFinal;
    private Long creditPaymentDays;
    //Only used for counter offer
    private Long negotiationHistoryId;
}
