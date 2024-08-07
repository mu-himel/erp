package com.agi.aesl.erpscm.inventory.dto.request;

import com.agi.aesl.erpscm.inventory.entity.ApproveStatus;
import lombok.Data;

@Data
public class ItemApproveRequestDto {
    private String code;
    private ApproveStatus approveStatus;
    private ItemMergeRequestDto itemMergeRequestDto;


}
