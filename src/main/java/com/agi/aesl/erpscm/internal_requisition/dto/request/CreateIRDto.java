package com.agi.aesl.erpscm.internal_requisition.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateIRDto {
    private Long id;
    private Long categoryId;
    private String priority;
    private Long warehouseId;
    private String irNo;
    List<CreateIRDetailDto> details;
}
