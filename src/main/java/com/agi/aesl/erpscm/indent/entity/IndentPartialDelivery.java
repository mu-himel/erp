package com.agi.aesl.erpscm.indent.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "indent_partial_deliveries")
public class IndentPartialDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private IndentDeliveryDetail indentDeliveryDetail;

    private LocalDate pdDate;

    private Long qty;
}
