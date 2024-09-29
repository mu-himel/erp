package com.agi.aesl.erpscm.cs.dto;

import com.agi.aesl.erpscm.cs.entity.CsVendorDetail;
import lombok.Data;

import java.util.List;

@Data
public class CsDetailReqDto {
    private Long id;
    private Long indentDetailId;
    private List<CsVendorDetailDto> vendors;
}
