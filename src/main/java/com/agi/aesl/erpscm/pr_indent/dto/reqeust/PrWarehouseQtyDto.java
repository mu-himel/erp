package com.agi.aesl.erpscm.pr_indent.dto.reqeust;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PrWarehouseQtyDto {

    protected Long id;
    protected Long warehouseId;
    protected BigDecimal orderQty;
    protected BigDecimal prQty;
    protected BigDecimal rfqQty;
    private List<PartialDeliveryTimeDto> partialDeliveries = new ArrayList<>();
}
