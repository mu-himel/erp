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

    @Enumerated(EnumType.STRING)
    private DeliveryCharge deliveryCharge;
    @Column(precision = 38,scale = 4)
    private BigDecimal deliveryChargeAmount;
    private Boolean mushakIncluded;
    private Boolean isVatAdded;
    @Column(precision = 38,scale = 4)
    private BigDecimal vatPercent;
    @Column(precision = 38,scale = 4)
    private BigDecimal aitPercent;
    @Column(precision = 38,scale = 4)
    private BigDecimal vatAmount;
    @Column(precision = 38,scale = 4)
    private BigDecimal aitAmount;
    private Boolean isAitAdded;
    @Column(precision = 38,scale = 4)
    private BigDecimal subTotalPrice;
    @Column(precision = 38,scale = 4)
    private BigDecimal totalPrice;
    private Long creditPaymentDuration;
    private String creditPaymentUnit;
    private String note;
}
