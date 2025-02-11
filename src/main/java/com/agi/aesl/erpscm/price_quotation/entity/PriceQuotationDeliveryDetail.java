package com.agi.aesl.erpscm.price_quotation.entity;

import com.agi.aesl.erpscm.common.enums.DeliveryCharge;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "price_quotation_delivery_details")
public class PriceQuotationDeliveryDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    private PriceQuotationDetail priceQuotationDetail;

    @ManyToOne
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    private DeliveryCharge DeliveryCharge;

    @Column(precision = 38,scale = 4)
    private BigDecimal deliveryOrderQty;

    @Column(precision = 38,scale = 4)
    private BigDecimal deliveryChargeAmount;
}
