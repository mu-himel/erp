package com.agi.aesl.erpscm.store_receive.service;

import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.repository.GrnDetailRepository;
import com.agi.aesl.erpscm.inventory.dto.response.ItemDetail;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.store_receive.dto.SrnDto;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveDetail;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveNote;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SrnStoreService {

    @Transactional
    public void setDetail(StoreReceiveNote storeReceiveNote, ItemService itemService, SrnDto srnDto,
                          List<String> ids, GrnDetailRepository grnDetailRepository){
        storeReceiveNote.setSrnDetails(srnDto.getSrnDetails().stream().map(storeReceiveDetailDto -> {
            StoreReceiveDetail storeReceiveDetail = new StoreReceiveDetail();
            Optional<Item> itemOp = itemService.getItemDetail(storeReceiveDetailDto.getItem().getId());
            if(itemOp.isEmpty()) {
                throw new AesException("Sorry! Item not found");
            }
            Optional<GoodReceiveItemDetail> goodReceiveItemDetailOptional = grnDetailRepository
                    .findById(storeReceiveDetailDto.getGoodReceiveItemDetail().getId());
            if(goodReceiveItemDetailOptional.isEmpty()){
                throw new AesException("Good Receive Detail not found");
            }
            Item item = itemOp.get();
            storeReceiveDetail.setItem(item);
            storeReceiveDetail.setWarehouse(storeReceiveDetailDto.getWarehouse());
            storeReceiveDetail.setWarehouseStore(storeReceiveDetailDto.getWarehouseStore());

            storeReceiveDetail.setGoodReceiveItemDetail(goodReceiveItemDetailOptional.get());
            storeReceiveDetail.setStoreReceiveNote(storeReceiveNote);
            storeReceiveDetail.setCostCenter(storeReceiveDetailDto.getCostCenter());
            BigDecimal stockInQty = storeReceiveDetailDto.getStockInQty()!=null? storeReceiveDetailDto
                    .getStockInQty(): new BigDecimal(0);
            storeReceiveDetail.setStockInQty(stockInQty);
            ids.add(item.getItemCategory().getId().toString());
            ids.add(item.getItemParentCategory().getId().toString());


            return storeReceiveDetail;
        }).toList());
    }

    @Transactional
    public void storeInItem(GoodReceiveNote grn, StoreReceiveDetail srnd, ItemService itemService, Long warehouseId) {
        Optional<Item> itemOp = itemService.getItemDetail(srnd.getItem().getId());
        if(itemOp.isEmpty()) {
            throw new AesException("Sorry! Item not found");
        }
        Item item = itemOp.get();
        Optional<ItemDetail> itemDetailOp = (Optional<ItemDetail>)itemService.getItemDetailWithWarehouseWithoutInTransit(item.getId());
        if(itemDetailOp.isPresent()) {
            grn.setIsReceivedByStore(true);
            grn.setGrnStatus(GrnStatus.COMPLETED);
            ItemDetail itemDetail = itemDetailOp.get();
            if(warehouseId!=null) {

                List<Map<String, Object>> stores = itemDetail
                        .getWarehouses().get(warehouseId+"");
                Optional<Map<String, Object>> store = stores.stream().filter(
                        stringObjectMap -> !((String) stringObjectMap.get("warehouseStoreName"))
                                .toLowerCase().contains("finish goods")
                ).findFirst();
                store.ifPresent(stringObjectMap -> {
                    Long warehouseStoreId = (Long) stringObjectMap.get("warehouseStoreId");
                    itemService.stockIn(item, srnd.getStockInQty(),
                            warehouseId,
                            warehouseStoreId);
                });
            }
        }
    }
}
