package com.agi.aesl.erpscm.demand.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.agi.aesl.erpscm.demand.enums.DemandPriority;
import com.agi.aesl.erpscm.demand.enums.DemandStatus;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
@Entity
@Table(name = "scm_demand_details")
public class DemandDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Demand demand;

    @ManyToOne(fetch = FetchType.LAZY)
    private ItemCategory itemCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "Category is Required")
    private ItemCategory itemParentCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    @ManyToOne
    private CategoryBrand brand;

    @NotNull(message = "Quantity is required")
    @Column(precision = 38,scale = 4)
    private BigDecimal requestQuantity;

    @Column(precision = 38,scale = 4)
    private BigDecimal approvedQuantity;

    @Column(precision = 38,scale = 4)
    private BigDecimal currentStock;

    private String specification;

    @Column(length = 500)
    private String receiveNote;

    @Column(length = 500)
    private String declineNote;

    @Column(length = 500)
    private String storeNote;

    @Enumerated(EnumType.STRING)
    private DemandPriority priority;

    @Enumerated(EnumType.STRING)
    private DemandStatus status;

    @Column(precision = 38,scale = 4)
    private BigDecimal prQty;


    private LocalDateTime sendToUserDate;

    private LocalDateTime receivedByUserDate;

    @OneToMany(mappedBy = "demandDetail", cascade = CascadeType.ALL)
    private List<DemandDetailAttribute> attributes;
}
