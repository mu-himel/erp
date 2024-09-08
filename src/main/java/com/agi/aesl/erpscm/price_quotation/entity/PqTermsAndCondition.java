package com.agi.aesl.erpscm.price_quotation.entity;

import com.agi.aesl.erpscm.indent.entity.Indent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pq_terms_and_conditions")
public class PqTermsAndCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private PriceQuotation priceQuotation;

    @ManyToOne
    @JsonIgnore
    private Indent rfq;

    private Long vendorId;

    private String termAndCondition;
}
