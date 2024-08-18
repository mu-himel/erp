package com.agi.aesl.erpscm.pr_indent.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import jakarta.persistence.*;
import lombok.Data;

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

    private Long prQty;

    private Long orderQty;

    @ManyToOne
    private PrIndentDetail prIndentDetail;

    @OneToMany(mappedBy = "prIndentWarehouseDetail", cascade = CascadeType.ALL)
    private List<PrIndentPartialDelivery> partialDeliveyTimes;
}
