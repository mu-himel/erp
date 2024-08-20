package com.agi.aesl.erpscm.goods_receive.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodReceiveItemDetailDto {
    private ReferenceObjectDto item;
    private ReferenceObjectDto category;
    private ReferenceObjectDto subCategory;
    private ReferenceObjectDto warehouse;
    private ReferenceObjectDto warehouseStore;
    private LocalDate manufactureDate;
    private LocalDate expireDate;
    private Long orderQty;

    public void setManufactureDate(String manufactureDate){
        this.manufactureDate = (manufactureDate!=null)?
                LocalDate.parse(manufactureDate): null;
    }

    public void setExpireDate(String expireDate){
        this.expireDate = (expireDate!=null)?
                LocalDate.parse(expireDate): null;
    }

}
