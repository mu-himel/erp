package com.agi.aesl.erpscm.demand.dto.request;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.agi.aesl.erpscm.comment.entity.CommentAttachment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandReceiveDto {
    private Long demandId;
        private BigDecimal qty;
        private Long demandDetailId;
        private Long itemId;
        private String note;
        private List<CommentAttachment> attachments = new ArrayList<>();
        private Long warehouseId;
        private Long warehouseStoreId;
}
