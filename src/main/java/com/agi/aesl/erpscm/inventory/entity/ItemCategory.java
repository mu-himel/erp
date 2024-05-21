package com.agi.aesl.erpscm.inventory.entity;


// import io.swagger.annotations.ApiModelProperty;
// import io.swagger.annotations.ApiParam;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scm_item_categories")
public class ItemCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(updatable = false)
  private Long id;

  private String name;

  @Column(unique = true, name="code")
  private String code;

  @ManyToOne
  private ItemCategory parentCategory;

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
  // @ApiModelProperty(hidden = true)
  private List<CategoryBudget> budgets;

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
  private List<CategoryAttribute> attributes;

  @OneToMany(mappedBy = "category",cascade = CascadeType.ALL)
  private List<CategoryBrand> brands;

  private Boolean active=true;

  private BigDecimal vat;

  private Long cpsCategoryId;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;


  public ItemCategory(Long id) {
    this.id = id;
  }
}


