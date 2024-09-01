package com.agi.aesl.erpscm.rfq.dto;

import lombok.Data;

import java.util.List;

@Data
public class RfqRequestDto {
    private Long id;
    private Integer duration;
    private Long subCategoryId;
    List<RfqItemDto> items;
}
