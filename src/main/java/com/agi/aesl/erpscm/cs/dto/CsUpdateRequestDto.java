package com.agi.aesl.erpscm.cs.dto;

import lombok.Data;

@Data
public class CsUpdateRequestDto {
    private Long csDetailId;
    private Long indentDetailId;
    private CsVendorDetailDto csVendorDetailDto;
}
