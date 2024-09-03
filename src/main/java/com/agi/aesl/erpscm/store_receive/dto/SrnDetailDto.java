package com.agi.aesl.erpscm.store_receive.dto;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveNote;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SrnDetailDto {

    private Long id;

    private String costCenter;

    private StoreReceiveNote storeReceiveNote;

    private GoodReceiveItemDetail goodReceiveItemDetail;


    private Item item;

    private BigDecimal stockInQty;


    private Warehouse warehouse;


    private WarehouseStore warehouseStore;
}
