package com.agi.aesl.erpscm.inventory.dto.request;

import lombok.Data;
@Data
public class PendingItemAttributeDto {
    private String attributeType;
    private String attributeValue;
    private String attributeUnit;
}
