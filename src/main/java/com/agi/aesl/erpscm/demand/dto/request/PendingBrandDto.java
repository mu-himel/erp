package com.agi.aesl.erpscm.demand.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PendingBrandDto {
    private String brandName;
    private ReferenceObjectDto subCategory;
}
