package com.agi.aesl.erpscm.inventory.user_request.entity;

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

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "user_categories")
@NoArgsConstructor
public class UserCategory extends VerifyableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    private String name;

    @Column(unique = true, name="code")
    private String code;

    @ManyToOne
    private UserCategory parentCategory;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<UserCategoryAttribute> attributes;

    @OneToMany(mappedBy = "category",cascade = CascadeType.ALL)
    private List<UserCategoryBrand> brands;

    private Boolean active=true;

    @Enumerated(EnumType.STRING)
    private UserCategoryStatus categoryStatus;

    @Enumerated(EnumType.STRING)
    private UserCategoryStatus reviewPrevStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    private Employee createdBy;

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
}
