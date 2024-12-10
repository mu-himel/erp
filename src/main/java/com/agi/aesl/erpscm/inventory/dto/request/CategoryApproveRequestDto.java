package com.agi.aesl.erpscm.inventory.dto.request;

import com.agi.aesl.erpscm.inventory.entity.ApproveStatus;
import lombok.Data;

@Data
public class CategoryApproveRequestDto {
    private String code;
    private Long scmParentCategoryId;
    private Boolean isMerged;
    private Long warehouseStoreId;
    private MergePendingCategoryDto mergePendingCategoryDto;
    private ApproveStatus approveStatus;
}
