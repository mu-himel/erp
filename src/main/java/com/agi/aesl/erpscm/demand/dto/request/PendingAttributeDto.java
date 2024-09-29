package com.agi.aesl.erpscm.demand.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import lombok.Data;

@Data
public class PendingAttributeDto {
    private ReferenceObjectDto subCategory;
    private String attributeType;
    private String attributeValue;
    private String attributeUnit;
}
