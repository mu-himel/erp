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
    private String vendorName;
    private Long vendorId;
    private String grnNo;
    private String deliveryCharge;
    private String mushak;
    private BigDecimal deliveryChargeAmount;
    private Integer days;
    private String vatOption;
    private String aitOption;
    private BigDecimal totalPrice;
    private BigDecimal vat;
    private BigDecimal vatPctg;
    private BigDecimal subTotal;
    private Long warehouseId;
    List<GrnManualItemDetailDto> grnDetails;


}
