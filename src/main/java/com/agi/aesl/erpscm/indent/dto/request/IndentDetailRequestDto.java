package com.agi.aesl.erpscm.indent.dto.request;

import com.agi.aesl.erpscm.pr_indent.dto.reqeust.PrWarehouseQtyDto;
import lombok.Data;

import java.util.List;

@Data
public class IndentDetailRequestDto {
    private Long id;
    private String attribute;
    private Long brandId;
    private String productRequirementsIds;
    private List<PrWarehouseQtyDto> warehouses;
    private Long subCategoryId;
}
