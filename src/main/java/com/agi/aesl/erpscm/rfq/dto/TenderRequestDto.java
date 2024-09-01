package com.agi.aesl.erpscm.rfq.dto;

import lombok.Data;

import java.util.List;

@Data
public class TenderRequestDto {
    private String code;
    private String rfqNo;
    private String deadline;
    private String itemCategoryCode;
    private List<TenderItemDto> tenderItems;
}
