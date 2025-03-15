package com.agi.aesl.erpscm.purchase_order.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrderWarehouseDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PoDetailReqDto {

    private ReferenceObjectDto csVendorDetail;
    private String itemName;
    private BigDecimal unitPrice;
    private BigDecimal deliveryQty;
    private BigDecimal remainingQty;
    private LocalDate deliveryDate;
    private String transactionType;
    private String estimatedDeliveryDays;
    private String creditDays;
    private BigDecimal totalPrice;
    private String warrantyUnit;
    private String warrantyDuration;
    private BigDecimal vatPercent;
    private BigDecimal vatAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal subTotal;
    private ReferenceObjectDto warehouse;
    private List<PurchaseOrderWarehouseDetail> warehouseDetailList;
    private Long categoryId;
    private Long subCategoryId;
}
