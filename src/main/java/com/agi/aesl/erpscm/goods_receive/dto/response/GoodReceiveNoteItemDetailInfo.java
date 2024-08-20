package com.agi.aesl.erpscm.goods_receive.dto.response;

import com.agi.aesl.erpscm.common.enums.DeliveryCharge;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Data
public class GoodReceiveNoteItemDetailInfo {
    private Long id;
    private LocalDate manufactureDate;
    private LocalDate expireDate;
    private LocalDate createdAt;
    private BigDecimal receiveQty;
    private Optional<?> item;
    private Integer estimatedDays;
    private String itemAttribute;
    private BigDecimal orderQty;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private DeliveryCharge deliveryCharge;
    private BigDecimal deliveryChargeAmount;
    private BigDecimal vatPercent;
    private BigDecimal vatAmount;
}
