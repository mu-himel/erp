package com.agi.aesl.erpscm.inventory.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RemoteCategoryRequestDto {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String code;

    private ReferenceObjectDto parentCategory;

    private List<CategoryAttribute> attributes;

    @JsonProperty(value = "brands")
    private List<String> brands;


    private BigDecimal vat;

    private Organization organization;

    private ReferenceObjectDto warehouse;
    private ReferenceObjectDto warehouseStore;

    private Long cpsCategoryId;

    private Long scmCategoryId;
}
