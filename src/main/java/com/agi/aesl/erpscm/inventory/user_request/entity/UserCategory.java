package com.agi.aesl.erpscm.inventory.user_request.entity;

import com.agi.aesl.erpscm.common.BrandInterface;
import com.agi.aesl.erpscm.common.CategoryAttributeInterface;
import com.agi.aesl.erpscm.common.CategoryInterface;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.indent.enums.IndentVerificationStatus;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.enums.CategoryStatus;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "user_categories")
@NoArgsConstructor
public class UserCategory extends VerifyableEntity implements CategoryInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    private String name;

    @Column(unique = true, name="code")
    private String code;

    @ManyToOne
    private UserCategory parentCategory;

    @ManyToOne
    private ItemCategory activeParentCategory;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<UserCategoryAttribute> attributes;

    @OneToMany(mappedBy = "category",cascade = CascadeType.ALL)
    private List<UserCategoryBrand> brands;

    private Boolean active=true;

    @ManyToOne
    private WarehouseStore store;

    private BigDecimal vat;

    private Boolean isApprovedByStore;
    private String mergedCategory;

    @Enumerated(EnumType.STRING)
    private UserCategoryStatus categoryStatus;

    @Enumerated(EnumType.STRING)
    private UserCategoryStatus reviewPrevStatus;

    @Column(length = 500)
    private String rejectNoteFromStore;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    private Employee createdBy;

    private Long cpsCategoryId;

    public UserCategory(CategoryRequestDto categoryRequestDto){
        this.name = categoryRequestDto.getName();
        this.code = categoryRequestDto.getCode();

    }

    @Override
    public void setStatus(String status){
        this.categoryStatus = UserCategoryStatus.valueOf(status);
    }

    public UserCategory(Long id) {
        this.id = id;
    }

    @Override
    public List<BrandInterface> getBrandInterfaces() {
        return this.brands.stream().map(b->{
            BrandInterface brandInterface = new CategoryBrand();
            brandInterface.setId(b.getId());
            brandInterface.setName(b.getName());
            return brandInterface;
        }).toList();
    }

    @Override
    public List<CategoryAttributeInterface> getAttributeInterfaces() {
        return this.attributes.stream().map(attr->{
            CategoryAttributeInterface cai = new UserCategoryAttribute();
            cai.setAttributeValue(attr.getAttributeValue());
            cai.setAttributeUnit(attr.getAttributeUnit());
            cai.setAttributeType(attr.getAttributeType());
            return cai;
        }).toList();
    }
}
