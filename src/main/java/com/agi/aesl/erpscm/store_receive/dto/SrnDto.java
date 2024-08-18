package com.agi.aesl.erpscm.store_receive.dto;

import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveDetail;
import lombok.Data;

import java.util.List;

@Data
public class SrnDto {
    private String srnNo;
    private String comment;
    List<StoreReceiveDetail> srnDetails;
}
