package com.agi.aesl.erpscm.demand.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PendingAttributeRemoteDto {
    private List<PendingAttributeDto> pendingAttributes;
}
