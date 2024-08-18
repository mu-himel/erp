package com.agi.aesl.erpscm.pr_indent.dto.reqeust;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PrWarehouseQtyDto {

    protected Long id;
    protected Long warehouseId;
    protected Long orderQty;
    protected Long prQty;
    protected Long rfqQty;
    private List<PartialDeliveryTimeDto> partialDeliveries = new ArrayList<>();
}
