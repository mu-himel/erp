package com.agi.aesl.erpscm.indent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@Table(name = "indent_partial_deliveries")
public class IndentPartialDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private IndentDeliveryDetail indentDeliveryDetail;

    private LocalDate pdDate;

    @Column(precision = 38, scale = 4)
    private BigDecimal qty;


    public IndentPartialDelivery(Long id) {
        this.id = id;
    }
}
