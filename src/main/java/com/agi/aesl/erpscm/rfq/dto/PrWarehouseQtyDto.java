package com.agi.aesl.erpscm.rfq.dto;

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
