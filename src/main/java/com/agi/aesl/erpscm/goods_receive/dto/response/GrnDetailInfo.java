package com.agi.aesl.erpscm.goods_receive.dto.response;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository.WarehouseInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GrnDetailInfo {
    private Long id;
    private String grnNo;
    private String grnStatus;
    private Long indentId;
    private String indentNo;
    private Boolean isReceivedByStore;
    private WarehouseInfo warehouse;
    private Employee createdBy;
    private List<GoodReceiveNoteItemDetailInfo> goodReceiveItemDetails;
    private Integer creditPaymentDuration;
    private String vendorName;
    private String vendorEmail;
    private String vendorPhoneNo;
    private Boolean mushakIncluded;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}
