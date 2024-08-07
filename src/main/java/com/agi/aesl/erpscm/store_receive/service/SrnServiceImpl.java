package com.agi.aesl.erpscm.store_receive.service;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import com.agi.aesl.erpscm.inventory.dto.response.ItemDetail;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.store_receive.dto.SrnDto;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveNote;
import com.agi.aesl.erpscm.store_receive.repository.SrnRepository;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SrnServiceImpl implements SrnService{

    @Autowired
    private SrnRepository srnRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private GrnService grnService;

    @Autowired
    private ClaimResolver claimResolver;

    @Override
    @Transactional
    public void addSrn(Jwt token, SrnDto srnDto) {
        claimResolver.setToken(token);
        StoreReceiveNote storeReceiveNote = new StoreReceiveNote();
        storeReceiveNote.setSrnNo(srnDto.getSrnNo());
        storeReceiveNote.setComment(srnDto.getComment());
        storeReceiveNote.setEmployee(new Employee(claimResolver.getEmployee().get().getId()));
        Optional<GoodReceiveNote> goodReceiveNoteOp = grnService.getByGrnNo(srnDto.getSrnNo());
        if(goodReceiveNoteOp.isEmpty()){
            throw new RuntimeException("Grn not found");
        }
        GoodReceiveNote grn = goodReceiveNoteOp.get();
        grn.setIsReceivedByStore(true);
        grn.setGrnStatus(GrnStatus.COMPLETED);
        storeReceiveNote.setGrn(grn);
        storeReceiveNote.setSrnDetails(srnDto.getSrnDetails().stream().map(storeReceiveDetail -> {
            Optional<Item> itemOp = itemService.getItemDetail(storeReceiveDetail.getItem().getId());
            if(itemOp.isEmpty()) {
                throw new RuntimeException("Sorry! Item not found");
            }
            Item item = itemOp.get();
            Optional<ItemDetail> itemDetailOp = (Optional<ItemDetail>)itemService.getItemDetailWithWarehouse(item.getId());
            if(itemDetailOp.isPresent()) {
                ItemDetail itemDetail = itemDetailOp.get();
                List<Map<String,Object>> stores = itemDetail
                        .getWarehouses().get(claimResolver.getEmployee().get().getWarehouseId().toString());
                Optional<Map<String,Object>> store = stores.stream().filter(
                        stringObjectMap -> !((String)stringObjectMap.get("warehouseStoreName"))
                                .toLowerCase().contains("finish goods")
                ).findFirst();
                store.ifPresent(stringObjectMap -> {
                    Long warehouseStoreId = (Long)stringObjectMap.get("warehouseStoreId");
                    itemService.stockIn(item, storeReceiveDetail.getStockInQty(),
                            claimResolver.getEmployee().get().getWarehouseId(),
                            warehouseStoreId);
                });

                storeReceiveDetail.setStoreReceiveNote(storeReceiveNote);
            }
            return storeReceiveDetail;
        }).collect(Collectors.toList()));
        srnRepository.save(storeReceiveNote);
    }

    @Override
    public List<?> getPendingDemandListBySrnItems(Long id) {
        return srnRepository.getPendingDemandsBySrnForSrnItems(id);
    }

    @Override
    public List<?> getPendingDemandListBySrnItems(String attributes) {
        return srnRepository.getPendingDemandsBySrnForSrnItems(attributes);
    }

    @Override
    public Page<?> getAll(Optional<Integer> page, Optional<Integer> size, Optional<String> fromDate, Optional<String> toDate) {
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + "T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get() + "T23:59:59");
        }
        return srnRepository.findAllSrnByStatus(GrnStatus.READY_FOR_STORE.toString(), fromDateObj, toDateObj,
                pageable);
    }

    @Override
    public Page<?> getAllComplete(Optional<Integer> page, Optional<Integer> size, Optional<String> fromDate, Optional<String> toDate) {
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + "T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get() + "T23:59:59");
        }
        return srnRepository.findAllSrnByStatus(GrnStatus.COMPLETED.toString(),
                fromDateObj, toDateObj,pageable);
    }
}
