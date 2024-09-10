package com.agi.aesl.erpscm.price_quotation.dto.request;

import lombok.Data;

@Data
public class CounterTermAndConditionDto {
    private String termsAndCondition;

    public CounterTermAndConditionDto(String tnc){
        this.termsAndCondition = tnc;
    }
}
