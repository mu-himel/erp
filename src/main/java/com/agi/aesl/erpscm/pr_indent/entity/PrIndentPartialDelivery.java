package com.agi.aesl.erpscm.pr_indent.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "pr_indent_partial_delivery_times")
public class PrIndentPartialDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private PrIndentWarehouseDetail prIndentWarehouseDetail;

    private LocalDate pdDate;

    @Column(precision = 38, scale = 4)
    private BigDecimal qty;
}
