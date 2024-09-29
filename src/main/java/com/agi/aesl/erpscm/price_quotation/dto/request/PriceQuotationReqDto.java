package com.agi.aesl.erpscm.price_quotation.dto.request;

import com.agi.aesl.erpscm.price_quotation.enums.CreditType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PriceQuotationReqDto {
    private Long rfqId;
    private String code;

    private CreditType paymentMethod;
    private Long remoteOfferId;
    private Long vendorId;
    private String vendorName;
    private String vendorEmail;
    private String vendorPhoneNo;
    private String vendorType;
    private Integer score;
    private Boolean isFinal;
    private Long negotiationHistoryId;
    private List<PriceQuotationDetailReqDto> details;
    private PriceQuotationSummaryDto priceQuotationSummary;
    private List<DeliveryDetailDto> warehouses;
    private List<String> termsAndConditions = new ArrayList<>();
    private String file;
}
