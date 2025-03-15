package com.agi.aesl.erpscm.inventory.user_request.entity;

import com.agi.aesl.erpscm.common.ItemAttributeInterface;
import com.agi.aesl.erpscm.common.ItemInterface;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.inventory.dto.request.UserItemRequestDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.entity.UserItemFunctionalUnit;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@Table(name = "user_items")
public class UserItem extends VerifyableEntity implements ItemInterface {


    @ManyToOne
    private ItemCategory category;

    @ManyToOne
    private ItemCategory subCategory;

    @Column(name = "code")
    private String code;

    private String name;

    private String itemAttributeName;

    private String itemUnit;

    @OneToMany(mappedBy = "userItem", cascade = CascadeType.ALL)
    private List<UserItemAttribute> attributes=new ArrayList<>();

    @OneToMany(mappedBy = "userItem", cascade = CascadeType.ALL)
    private List<UserItemFunctionalUnit> functionalUnits = new ArrayList<>();

    private Boolean active=true;

    private Boolean isApprovedByStore;

    private String mergedItem;

    @Column(length = 500)
    private String rejectNoteFromStore;

    @ManyToOne
    private CategoryBrand brand;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    private Employee createdBy;

    @ManyToOne
    private Warehouse warehouse;

    @ManyToOne
    private WarehouseStore warehouseStore;

    @Enumerated(EnumType.STRING)
    private UserCategoryStatus itemStatus;

    @Enumerated(EnumType.STRING)
    private UserCategoryStatus reviewPrevStatus;

    @Override
    public void setStatus(String status){
        this.itemStatus = UserCategoryStatus.valueOf(status);
    }

    public UserItem(UserItemRequestDto itemRequestDto) {
        this.name = itemRequestDto.getName();
        this.code = itemRequestDto.getCode();
        this.itemUnit = itemRequestDto.getItemUnit();
        this.attributes = itemRequestDto.getAttributes().stream()
                .map(attr-> new UserItemAttribute(attr,this)).toList();

        this.functionalUnits = itemRequestDto.getFunctionalUnits().stream()
                .map(unit->new UserItemFunctionalUnit(unit,this))
                .toList();
    }

    public List<ItemAttributeInterface> getItemAttributes() {
        return this.getAttributes().stream().map(attr->{
            ItemAttributeInterface iAttr = new UserItemAttribute();
            iAttr.setAttributeType(attr.getAttributeType());
            iAttr.setAttributeUnit(attr.getAttributeUnit());
            iAttr.setAttributeValue(attr.getAttributeValue());
            return iAttr;
        }).toList();
    }
}
