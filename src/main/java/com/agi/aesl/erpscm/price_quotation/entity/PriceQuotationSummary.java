package com.agi.aesl.erpscm.price_quotation.entity;

import com.agi.aesl.erpscm.common.enums.DeliveryCharge;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "price_quotation_summary")
public class PriceQuotationSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private PriceQuotation priceQuotation;

    private DeliveryCharge deliveryCharge;
    private BigDecimal deliveryChargeAmount;
    private Boolean mushakIncluded;
    private Boolean isVatAdded;
    private BigDecimal vatPercent;
    private BigDecimal vatAmount;
    private Boolean isAitAdded;
    private BigDecimal subTotalPrice;
    private BigDecimal totalPrice;
    private Long creditPaymentDuration;
    private String creditPaymentUnit;
    private String note;
}
