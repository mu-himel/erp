package com.agi.aesl.erpscm.goods_receive.service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.goods_receive.dto.request.GoodReceiveItemDetailDto;
import com.agi.aesl.erpscm.goods_receive.dto.request.GoodReceiveNoteDto;

import com.agi.aesl.erpscm.goods_receive.dto.request.GrnManualRequestDto;

import com.agi.aesl.erpscm.goods_receive.dto.response.GoodReceiveNoteItemDetailInfo;
import com.agi.aesl.erpscm.goods_receive.dto.response.GrnDetailInfo;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnMode;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.repository.GrnDetailRepository;
import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository;
import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository.*;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.quality_control.service.QcService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GrnServiceImpl implements GrnService{

    @Autowired
    private GrnRepository grnRepository;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private IntegrationReaderService integrationReaderService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private QcService qcService;

    @Autowired
    private GrnDetailRepository grnDetailRepository;

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
                            goodReceiveItemDetail.setReceiveQty(BigDecimal.valueOf(goodReceiveItemDetailDto.getOrderQty()));
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
    @Transactional
    public void createManualGrn(Jwt token, GrnManualRequestDto grnManualDto) {
        claimResolver.setToken(token);
        if(claimResolver.getEmployee()==null){
            throw new RuntimeException("Sorry! Employee profile required");
        }
        GoodReceiveNote grn = new GoodReceiveNote();
        grn.setGrnDate(LocalDate.now());
        grn.setGrnNo(grnManualDto.getGrnNo());
        grn.setIndentNo(grnManualDto.getIndentNo() );
        grn.setGrnMode(GrnMode.MANUAL);
        grn.setGrnStatus(GrnStatus.PENDING_QC);

        grn.setWarehouse(new Warehouse(grnManualDto.getWarehouseId()));
        if(token != null){
            grn.setCreatedBy(claimResolver.getEmployee().get());
        }


        grn.setGoodReceiveItemDetails(grnManualDto.getGrnDetails().stream().map(detailDto->{
            GoodReceiveItemDetail grid = new GoodReceiveItemDetail();

                Optional<Item> itemOp = itemService.getItemDetail(detailDto.getItem().getId());
                if(itemOp.isPresent()){
                    Item item = itemOp.get();
                    grid.setItem(item);
                    grid.setBrandName(item.getName());
                    grid.setCategory(item.getItemParentCategory());
                    grid.setSubCategory(item.getItemCategory());
                    grid.setEstimatedDeliveryDays(detailDto.getEstDeliveryDays());
                    grid.setReceiveQty(detailDto.getOrderQty());
                    grid.setPricePerUnit(detailDto.getPricePerUnit());
                    grid.setWarehouse(new Warehouse(grnManualDto.getWarehouseId()));
                    grid.setGoodReceiveNote(grn);
                }

                return grid;
            }).collect(Collectors.toList())
        );

        grn.setAitOption(grnManualDto.getAitOption());
        grn.setVatOption(grnManualDto.getVatOption());
        grn.setVat(grnManualDto.getTotalVat());
        grn.setDeliveryChargeAmount(grnManualDto.getDeliveryChargeAmount());
        grn.setDeliveryCharge(grnManualDto.getDeliveryCharge());
        grn.setDays(grnManualDto.getDays());
        grn.setInvoicePath(grnManualDto.getInvoicePath());
        grn.setSubTotal(grnManualDto.getInTotal());
        grn.setTotalPrice(grnManualDto.getTotalPrice());

        grn.setVendorId(grnManualDto.getVendor().getId());
        grn.setVendorName(grnManualDto.getVendor().getName());
        grn.setVendorPhone(grnManualDto.getVendor().getVendorPhone());
        grn.setVendorEmail(grnManualDto.getVendor().getVendorEmail());

        grn.setMushak(grnManualDto.getMushak());
        grn.setPaymentType(grnManualDto.getPayment());
        grnRepository.save(grn);
    }

    @Override
    public Page<?> getAllGrn(Optional<Integer> page,
                             Optional<Integer> size, Optional<String> grnNo,
                             Optional<Integer> qty,Optional<Integer> receivedQty,
                             Optional<String> fromDate, Optional<String> toDate) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()){
            fromDateObj = LocalDateTime.parse(fromDate.get()+"T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get()+"T23:59:59");
        }
        return grnRepository.findAllGrn(pageable,grnNo.orElse(null),
                qty.orElse(null), receivedQty.orElse(null),
                fromDateObj,toDateObj);
    }

    @Override
    public Optional<?> getGrnById(Long id, Boolean returnTypeEntity) {
        if(returnTypeEntity){
            return grnRepository.findById(id);
        }else{
            Optional<GoodReceiveNoteDetailInfo> goodReceiveItemDetailOp = grnRepository.findGrnById(id);
            Map<String, Object> _detailInfo = new HashMap<>();
            if(goodReceiveItemDetailOp.isPresent()) {
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
                    if(detailInfo.getGrnMode().equals(GrnMode.AUTO)) {

//                        Optional<PriceQuotationDetailInfo> pqDetailOp = priceQuotationDetailRepository.findByItemAttribute(
//                                vendorId,
//                                goodReceiveNoteItemDetailInfo.getWarehouse().getId(),
//                                goodReceiveNoteItemDetailInfo.getItemAttribute()
//                        );
//                    if (pqDetailOp.isPresent()) {
//                        PriceQuotationDetailInfo pqd = pqDetailOp.get();
//                        String itemAttribute = pqd.getItemAttribute() + " - " + pqd.getExtendedAttributes();
//
//                        grnidi.setEsitmatedDays(pqd.getEstDeliveryDays());
//                        grnidi.setTotalPrice(pqd.getTotalPrice());
//                        grnidi.setUnitPrice(pqd.getUnitPrice());
//                        grnidi.setItemAttribute(itemAttribute);
//                        grnidi.setVatPercent(pqd.getVatPercent());
//                        grnidi.setVatAmount(pqd.getVatAmount());
//
//                        grnidi.setDeliveryCharge(pqd.getDeliveryCharge().equals("Included") ? DeliveryCharge.Included :
//                                DeliveryCharge.Excluded);
//                        grnidi.setDeliveryChargeAmount(pqd.getDeliveryChargeAmount());
//                        if (pqd.getDeliveryOrderQty() != null) {
//                            grnidi.setOrderQty(pqd.getDeliveryOrderQty());
//                        } else {
//                            grnidi.setOrderQty(pqd.getRfqQty());
//                        }
//                        grnDetailInfo.setMushakIncluded(pqd.getMushakIncluded());
//                        grnDetailInfo.setIndentId(pqd.getIndentId());
//                        grnDetailInfo.setVendorName(pqd.getVendorName());
//                        grnDetailInfo.setVendorEmail(pqd.getVendorEmail());
//                        grnDetailInfo.setVendorPhoneNo(pqd.getVendorPhoneNo());
//                        grnDetailInfo.setCreditPaymentDuration(pqd.getCreditPaymentDuration());
//                    }

                    }
                    grnidi.setCreatedAt(goodReceiveNoteItemDetailInfo.getCreatedAt());
                    grnidi.setId(goodReceiveNoteItemDetailInfo.getId());
                    grnidi.setReceiveQty(goodReceiveNoteItemDetailInfo.getReceiveQty());
                    grnidi.setExpireDate(goodReceiveNoteItemDetailInfo.getExpireDate());
                    grnidi.setManufactureDate(goodReceiveNoteItemDetailInfo.getManufactureDate());
                    grnidi.setItem(itemService.getItemDetailWithWarehouse(goodReceiveNoteItemDetailInfo.getItem().getId()));
                    detailInfos.add(grnidi);
                });
                grnDetailInfo.setGoodReceiveItemDetails(detailInfos);

                _detailInfo.put("detailInfo", grnDetailInfo);
                _detailInfo.put("qcResult", qcService.getQcResultByGrn(detailInfo.getId()));
            }
            return Optional.ofNullable(_detailInfo);
            }


    }



    @Override
    public Page<?> getAllGrnPendingQC(Optional<Integer> page, Optional<Integer> size,
                                      Optional<String> grnNo, Optional<Integer> qty, Optional<Integer> receivedQty,
                                      Optional<String> fromDate, Optional<String> toDate) {
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + "T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get() + "T23:59:59");
        }
        return grnRepository.findAllGrnByStatus(
                grnNo.orElse(null), qty.orElse(null), receivedQty.orElse(null),
                GrnStatus.PENDING_QC.toString(),
                fromDateObj,
                toDateObj,pageable);
    }

    @Override
    public void updateGrnItemDetail(GoodReceiveItemDetail goodReceiveItemDetail) {
        grnDetailRepository.save(goodReceiveItemDetail);
    }

    @Override
    public Optional<?> getGRNById(Long id, boolean returnTypeEntity) {
        if(returnTypeEntity){
            return grnRepository.findById(id);
        }else{

            Optional<GrnRepository.GoodReceiveNoteDetailInfo> goodReceiveItemDetailOp = grnRepository.findGrnById(id);
            Map<String, Object> _detailInfo = new HashMap<>();
            if(goodReceiveItemDetailOp.isPresent()) {
                GrnRepository.GoodReceiveNoteDetailInfo detailInfo =  goodReceiveItemDetailOp.get();

                GrnDetailInfo grnDetailInfo = new GrnDetailInfo();
//                grnDetailInfo.setId(detailInfo.getId());
//                grnDetailInfo.setGrnNo(detailInfo.getGrnNo());
//                grnDetailInfo.setGrnStatus(detailInfo.getGrnStatus());
//                grnDetailInfo.setWarehouse(detailInfo.getWarehouse());
//                grnDetailInfo.setIsReceivedByStore(detailInfo.getIsReceivedByStore());
//                grnDetailInfo.setCreatedBy(detailInfo.getCreatedBy());
//                List<GoodReceiveNoteItemDetailInfo> detailInfos = new ArrayList<>();
//                detailInfo.getGoodReceiveItemDetails().stream().forEach(goodReceiveNoteItemDetailInfo -> {
//                    GoodReceiveNoteItemDetailInfo grnidi = new GoodReceiveNoteItemDetailInfo();
//                    grnidi.setId(goodReceiveNoteItemDetailInfo.getId());
//                    grnidi.setReceiveQty(goodReceiveNoteItemDetailInfo.getReceiveQty());
//                    grnidi.setExpireDate(goodReceiveNoteItemDetailInfo.getExpireDate());
//                    grnidi.setManufactureDate(goodReceiveNoteItemDetailInfo.getManufactureDate());
//                    grnidi.setItem(itemService.getItemDetailWithWarehouse(goodReceiveNoteItemDetailInfo.getItem().getId()));
//                    detailInfos.add(grnidi);
//                });
//                grnDetailInfo.setGoodReceiveNoteItemDetailInfoList(detailInfos);

                _detailInfo.put("detailInfo",detailInfo);
                _detailInfo.put("qcResult",qcService.getQcResultByGrn(detailInfo.getId()));


            }
            return Optional.ofNullable(_detailInfo);
        }
    }

    @Override
    public Optional<GoodReceiveNote> getByGrnNo(String grnNo) {
        return grnRepository.findByGrnNo(grnNo);
    }
}
