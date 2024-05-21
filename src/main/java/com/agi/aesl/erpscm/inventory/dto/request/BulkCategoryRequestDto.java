package com.agi.aesl.erpscm.inventory.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class BulkCategoryRequestDto {
    private Long userId;
    private List<CategoryRequestDtoCustom> categories;
}
