package com.agi.aesl.erpscm.pr_indent.dto.reqeust;

import lombok.Data;

import java.util.List;

@Data
public class PrIndentDetailRequestDto {
    private String attribute;
    private Long brandId;
    private List<PrWarehouseQtyDto> warehouses;
    private String productRequirementsIds;


}
