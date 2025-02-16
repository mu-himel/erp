package com.agi.aesl.erpscm.pr_indent.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "pr_indent_warehouses")
public class PrIndentWarehouseDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Warehouse warehouse;

    @Column(precision = 38,scale = 4)
    private BigDecimal prQty;

    @Column(precision = 38, scale = 4)
    private BigDecimal orderQty;

    @ManyToOne
    private PrIndentDetail prIndentDetail;

    @OneToMany(mappedBy = "prIndentWarehouseDetail", cascade = CascadeType.ALL)
    private List<PrIndentPartialDelivery> partialDeliveyTimes;
}
