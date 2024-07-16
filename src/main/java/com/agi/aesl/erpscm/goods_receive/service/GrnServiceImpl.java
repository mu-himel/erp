package com.agi.aesl.erpscm.goods_receive.service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.goods_receive.dto.request.GoodReceiveNoteDto;
import com.agi.aesl.erpscm.goods_receive.dto.response.GoodReceiveNoteItemDetailInfo;
import com.agi.aesl.erpscm.goods_receive.dto.response.GrnDetailInfo;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository;
import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository.*;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GrnServiceImpl implements GrnService{

    @Autowired
    private GrnRepository grnRepository;

    @Autowired
    private ClaimResolver claimResolver;

    @Override
    public String getNextGrnNumber() {
        Optional<Long> grnOp = grnRepository.findMaxOrderById();
        if(grnOp.isPresent()){
            Long grnNo = grnOp.get();
            Long newGrnNo = grnNo +1;
            return String.format("%05d",newGrnNo);
        }
        return String.format("%05d",1);
    }

    @Override
    @Transactional
    public void addGrn(Jwt token, GoodReceiveNoteDto goodReceiveNoteDto) {
        claimResolver.setToken(token);
        GoodReceiveNote goodReceiveNote = new GoodReceiveNote();
        goodReceiveNote.setGrnNo(goodReceiveNote.getGrnNo());
        goodReceiveNote.setGrnStatus(GrnStatus.PENDING_QC);
        if(token != null)
            goodReceiveNote.setCreatedBy(claimResolver.getEmployee().get());

        goodReceiveNote.setGoodReceiveItemDetails(
                goodReceiveNoteDto.getGoodReceiveItemDetails().stream().map(
                        goodReceiveItemDetailDto -> {
                            goodReceiveNote.setWarehouse(new Warehouse(goodReceiveItemDetailDto.getWarehouse().getId()));

                            GoodReceiveItemDetail goodReceiveItemDetail = new GoodReceiveItemDetail();
                            goodReceiveItemDetail.setItem(new Item(goodReceiveItemDetailDto.getItem().getId()));
                            goodReceiveItemDetail.setCategory(new ItemCategory(goodReceiveItemDetailDto.getCategory().getId()));
                            goodReceiveItemDetail.setSubCategory(new ItemCategory(goodReceiveItemDetailDto.getSubCategory().getId()));
                            goodReceiveItemDetail.setWarehouse(new Warehouse(goodReceiveItemDetailDto.getWarehouse().getId()));
                            if(goodReceiveItemDetailDto.getWarehouseStore()!=null){
                                goodReceiveItemDetail.setWarehouseStore(new WarehouseStore(goodReceiveItemDetailDto.getWarehouseStore().getId()));
                            }
                            goodReceiveItemDetail.setReceiveQty(BigDecimal.valueOf(goodReceiveItemDetailDto.getReceiveQty()));
                            if(goodReceiveItemDetailDto.getManufactureDate()!=null){
                                goodReceiveItemDetail.setManufactureDate(goodReceiveItemDetailDto.getManufactureDate());
                            }
                            if(goodReceiveItemDetailDto.getExpireDate()!=null){
                                goodReceiveItemDetail.setExpireDate(goodReceiveItemDetailDto.getExpireDate());
                            }
                            goodReceiveItemDetail.setGoodReceiveNote(goodReceiveNote);
                            return goodReceiveItemDetail;
                        }
                ).collect(Collectors.toList())
        );
        grnRepository.save(goodReceiveNote);
    }

    @Override
    public Page<?> getAllGrn(Optional<Integer> page,
                             Optional<Integer> size,
                             Optional<String> fromDate, Optional<String> toDate) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()){
            fromDateObj = LocalDateTime.parse(fromDate.get()+"T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get()+"T23:59:59");
        }
        return grnRepository.findAllGrn(pageable,fromDateObj,toDateObj);
    }

    @Override
    public Optional<?> getGrnById(Long id, Boolean returnTypeEntity) {
        if(returnTypeEntity){
            return grnRepository.findById(id);
        }else{
            Optional<GoodReceiveNoteDetailInfo> goodReceiveItemDetailOp = grnRepository.findGrnById(id);
            Map<String,Object> _detailinfo = new HashMap<>();
            if(goodReceiveItemDetailOp.isPresent()){
                GoodReceiveNoteDetailInfo detailInfo = goodReceiveItemDetailOp.get();
                GrnDetailInfo grnDetailInfo = new GrnDetailInfo();
                grnDetailInfo.setIndentNo(detailInfo.getIndentNo());
                grnDetailInfo.setCreatedAt(detailInfo.getCreatedAt());
                grnDetailInfo.setId(detailInfo.getId());
                grnDetailInfo.setGrnNo(detailInfo.getGrnNo());
                grnDetailInfo.setGrnStatus(detailInfo.getGrnStatus());
                grnDetailInfo.setWarehouse(detailInfo.getWarehouse());
                grnDetailInfo.setIsReceivedByStore(detailInfo.getIsReceivedByStore());
                grnDetailInfo.setCreatedBy(detailInfo.getCreatedBy());
                Long vendorId = detailInfo.getVendorId();
                List<GoodReceiveNoteItemDetailInfo> detailInfos = new ArrayList<>();
                detailInfo.getGoodReceiveItemDetails().stream().forEach(goodReceiveNoteItemDetailInfo -> {
                    GoodReceiveNoteItemDetailInfo grnidi = new GoodReceiveNoteItemDetailInfo();

//                    Optional<PriceQuotationDetailInfo> pqDetailOp = priceQuotationDetailRepository.findByItemAttribute(
//                            vendorId,
//                            goodReceiveNoteItemDetailInfo.getWarehouse().getId(),
//                            goodReceiveNoteItemDetailInfo.getItemAttribute()
//                    );
                });

            }
            return  goodReceiveItemDetailOp;
        }
    }
}
