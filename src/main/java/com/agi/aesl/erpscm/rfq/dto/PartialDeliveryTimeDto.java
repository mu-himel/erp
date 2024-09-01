package com.agi.aesl.erpscm.rfq.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PartialDeliveryTimeDto {
    private Long id;
    private LocalDate pdDate;
    private Long qty;
}
