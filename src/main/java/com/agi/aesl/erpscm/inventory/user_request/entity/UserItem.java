package com.agi.aesl.erpscm.inventory.user_request.entity;

import com.agi.aesl.erpscm.common.ItemAttributeInterface;
import com.agi.aesl.erpscm.common.ItemInterface;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_items")
public class UserItem extends VerifyableEntity implements ItemInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserCategory subCategory;

    @Column(name = "code")
    private String code;

    private String name;

    private String itemAttributeName;

    private String itemUnit;

    @OneToMany(mappedBy = "userItem", cascade = CascadeType.ALL)
    private List<UserItemAttribute> attributes=new ArrayList<>();

    private Boolean active=true;

    @ManyToOne
    private UserCategoryBrand brand;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    private Employee createdBy;

    @Enumerated(EnumType.STRING)
    private UserCategoryStatus itemStatus;

    @Enumerated(EnumType.STRING)
    private UserCategoryStatus reviewPrevStatus;

    @Override
    public void setStatus(String status){
        this.itemStatus = UserCategoryStatus.valueOf(status);
    }

    public UserItem(ItemRequestDto itemRequestDto) {
        this.name = itemRequestDto.getName();
        this.code = itemRequestDto.getCode();
        this.itemUnit = itemRequestDto.getItemUnit();
        this.attributes = itemRequestDto.getAttributes().stream()
                .map(attr-> new UserItemAttribute(attr,this)).collect(Collectors.toList());
    }

    public List<ItemAttributeInterface> getItemAttributes() {
        return this.getAttributes().stream().map((attr)->{
            ItemAttributeInterface iattr = new UserItemAttribute();
            iattr.setAttributeType(attr.getAttributeType());
            iattr.setAttributeUnit(attr.getAttributeUnit());
            iattr.setAttributeValue(attr.getAttributeValue());
            return iattr;
        }).collect(Collectors.toList());
    }
}
