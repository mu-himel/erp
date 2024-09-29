package com.agi.aesl.erpscm.cs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@Entity
@Table(name = "cs_vendor_delivery_details")
public class CsDeliveryDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private CsVendorDetail vendorDeliveryDetail;

    private Long warehouseId;
    private BigDecimal deliveryQty;
    private LocalDate deliveryDate;
}
