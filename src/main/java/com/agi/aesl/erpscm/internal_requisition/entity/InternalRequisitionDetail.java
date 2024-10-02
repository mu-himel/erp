package com.agi.aesl.erpscm.internal_requisition.entity;

import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "internal_requisition_details")
public class InternalRequisitionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private InternalRequisition ir;

    @ManyToOne
    private ItemCategory subCategory;

    private String itemAttribute;

    @ManyToOne
    private CategoryBrand brand;

    @ManyToOne
    private Item item;

    private BigDecimal qty;

    private String description;

    @OneToMany(mappedBy = "internalRequisitionDetail",
    cascade = CascadeType.ALL)
    List<InternalRequisitionDetailWarehouse> warehouses;
}
