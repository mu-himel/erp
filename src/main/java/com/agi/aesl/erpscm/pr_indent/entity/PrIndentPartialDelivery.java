package com.agi.aesl.erpscm.pr_indent.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

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

    private Long qty;
}
