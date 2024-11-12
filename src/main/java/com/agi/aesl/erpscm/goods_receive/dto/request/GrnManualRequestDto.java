package com.agi.aesl.erpscm.goods_receive.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import lombok.Data;
import org.hibernate.engine.jdbc.batch.spi.Batch;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GrnManualRequestDto {
    private String indentNo;
    private ReferenceObjectDto category;
    private VendorDto vendor;
    private String grnNo;
    private String deliveryCharge;
    private String mushak;
    private Long poId;
    private BigDecimal deliveryChargeAmount;
    private Integer days;
    private String vatOption;
    private String aitOption;
    private BigDecimal totalPrice;
    private BigDecimal totalVat;
    private BigDecimal vatPctg;
    private BigDecimal inTotal;
    private Long warehouseId;
    private String payment;
    private String invoicePath;
    List<GrnManualItemDetailDto> grnDetails;


}
