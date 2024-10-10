package com.agi.aesl.erpscm.internal_requisition.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateIRDetailDto {
    Long id;
    private Long subCategoryId;

    private String itemAttribute;

    private Long itemId;

    private Long brandId;

    private BigDecimal qty;

    private String description;

    private List<CreateIRWarehouseReqDto> warehouses;
}
