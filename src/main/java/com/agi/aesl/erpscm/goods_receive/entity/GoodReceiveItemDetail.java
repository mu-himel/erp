package com.agi.aesl.erpscm.goods_receive.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.goods_receive.enums.QcType;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "good_receive_item_details")
public class GoodReceiveItemDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    private GoodReceiveNote goodReceiveNote;

    @ManyToOne
    private Item item;

    private String brandName;

    private String itemAttributeName;

    @ManyToOne
    private ItemCategory category;

    @ManyToOne
    private ItemCategory subCategory;

    @ManyToOne
    private Warehouse warehouse;

    @ManyToOne
    private WarehouseStore warehouseStore;

    private LocalDate manufactureDate;

    private LocalDate expireDate;

    private BigDecimal receiveQty;

    private QcType qcType;

    private BigDecimal declaredQty;

    private BigDecimal inspectedQty;

    private BigDecimal totalApprovedQty;

    private BigDecimal totalDeclinedQty;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate createdAt;
}
