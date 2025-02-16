package com.agi.aesl.erpscm.cs.entity;

import com.agi.aesl.erpscm.price_quotation.entity.PriceQuotation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "cs_vendor_details")
@NoArgsConstructor
@Data
public class CsVendorDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private CsDetail csDetail;

    private Long vendorId;

    @ManyToOne
    private PriceQuotation priceQuotation;

    private String transactionType;
    @Column(precision = 38, scale = 4)
    private BigDecimal orderQty;
    @Column(precision = 38, scale = 4)
    private BigDecimal vatAmount;
    @Column(precision = 38, scale = 4)
    private BigDecimal discountAmount;
    @Column(precision = 38, scale = 4)
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "vendorDeliveryDetail", cascade = CascadeType.ALL)
    private List<CsDeliveryDetail> vendorDeliveryDetails;

    public CsVendorDetail(Long id){
        this.id = id;
    }
}
