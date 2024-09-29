package com.agi.aesl.erpscm.inventory.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import lombok.Data;
@Data
public class PendingItemAttributeDto {
    private String attributeType;
    private String attributeValue;
    private String attributeUnit;
}
