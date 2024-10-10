package com.agi.aesl.erpscm.internal_requisition.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateIRDetailDto {
    Long id;
    List<CreateIRDetailDto> details;
}
