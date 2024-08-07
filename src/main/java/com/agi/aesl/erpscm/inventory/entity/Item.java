package com.agi.aesl.erpscm.inventory.entity;


import com.agi.aesl.erpscm.inventory.enums.ItemInactiveStatus;
import com.agi.aesl.erpscm.inventory.enums.ItemUnit;

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
// import com.agi.aesl.erpscm.user_management.entity.User;
// import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "scm_items")
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ItemCategory itemCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    private ItemCategory itemParentCategory;

    @Column(name = "code")
    private String code;

    private String name;

    private String itemAttributeName;

    private String sku;

    private String manufacturer;

    @OneToMany(mappedBy = "item",cascade = CascadeType.ALL)
    // @ApiModelProperty(hidden = true)
    private List<ItemStock> stocks;

    private String itemUnit;

    private Integer stockThresholdQty;
    private Integer reorderPercentage;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<ItemAttribute> attributes=new ArrayList<>();

    private Boolean active=true;

    @ManyToOne
    private CategoryBrand brand;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // @ManyToOne
    private String createdBy;

    @Enumerated(EnumType.STRING)
    private ItemInactiveStatus itemInactiveStatus;


    public Item(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

}
