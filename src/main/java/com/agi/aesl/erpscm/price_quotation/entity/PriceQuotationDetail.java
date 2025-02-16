package com.agi.aesl.erpscm.price_quotation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "price_quotation_details")
public class PriceQuotationDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    private PriceQuotation priceQuotation;

    @OneToMany(mappedBy = "priceQuotationDetail",cascade =  CascadeType.ALL)
    private List<PriceQuotationDeliveryDetail> deliveryDetails;

    private String brandName;

    private String itemAttribute;

    private String extendedAttributes;

    @Column(precision = 38,scale = 4)
    private BigDecimal rfqQty;

    @Column(precision = 38,scale = 4)
    private BigDecimal unitPrice;

    @Column(precision = 38,scale = 4)
    private BigDecimal totalPrice;

    private Integer estDeliveryDays;

    private Integer warrantyDuration;
    private String warrantyUnit;
}
