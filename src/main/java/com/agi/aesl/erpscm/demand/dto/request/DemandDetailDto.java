package com.agi.aesl.erpscm.demand.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.demand.entity.DemandDetailAttribute;
import com.agi.aesl.erpscm.demand.enums.DemandPriority;

import lombok.Data;

@Data
public class DemandDetailDto {
    private Long id;
    private ReferenceObjectDto item;
    private ReferenceObjectDto brand;
    private ReferenceObjectDto subCategory;
    private ReferenceObjectDto category;
    private BigDecimal requestQuantity;
    private String specification;
    private DemandPriority priority;

    private List<DemandDetailAttribute> attributes;
}
