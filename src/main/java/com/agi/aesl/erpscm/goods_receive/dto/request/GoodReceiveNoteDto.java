package com.agi.aesl.erpscm.goods_receive.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class GoodReceiveNoteDto {
    Long remoteOfferId;
    Long warehouseId;
    String grnNo;
    private List<GoodReceiveItemDetailDto> goodReceiveItemDetails;
}
