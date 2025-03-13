package com.agi.aesl.erpscm.cs.dto;

import lombok.Data;

import java.util.List;

@Data
public class CsDetailReqDto {
    private Long id;
    private Long indentDetailId;
    private List<CsVendorDetailDto> vendors;
}
