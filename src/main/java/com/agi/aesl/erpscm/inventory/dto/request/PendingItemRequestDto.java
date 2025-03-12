package com.agi.aesl.erpscm.inventory.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PendingItemRequestDto {
    private Long scmItemId;
    private String subCategoryCode;
    private String brand;
    private String requestedBy;
    private String employeeId;
    private String reportingManager;
    private String designation;
    private String department;
    private Long warehouseId;
    private Long warehouseStoreId;
    private String warehouseName;
    private Long organizationId;
    private String warehouseLocation;
    private String itemAttributeName;
    private String extendedAttributes;
    private String itemUnit;
    private String code;
    private List<PendingItemAttributeDto> attributes;
}
