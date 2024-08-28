package com.agi.aesl.erpscm.indent.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "indent_delivery_details")
public class IndentDeliveryDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private IndentDetail indentDetail;

    @ManyToOne
    private Warehouse warehouse;

    private Long prQty;

    private Long orderQty;

    private Long rfqQty;

    @OneToMany(mappedBy = "indentDeliveryDetail", cascade = CascadeType.ALL)
    private List<IndentPartialDelivery> partialDeliveries;

    public IndentDeliveryDetail(Long id) {
        this.id =id;
    }
}
