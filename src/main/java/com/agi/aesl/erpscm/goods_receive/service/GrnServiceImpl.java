package com.agi.aesl.erpscm.goods_receive.service;

import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
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
import com.agi.aesl.erpscm.inventory.entity.ItemStock;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.quality_control.repository.QcQuery;
import com.agi.aesl.erpscm.quality_control.service.QcMailService;
import com.agi.aesl.erpscm.quality_control.service.QcService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private QcMailService qcMailService;

    @Autowired
    private OrgService orgService;

    @Autowired
    private CpsServerConfig cpsConfig;

    @Autowired
    private NetworkService networkService;

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

//    @Override
//    @Transactional
//    public void addGrn(Jwt token, GoodReceiveNoteDto goodReceiveNoteDto) {
//        claimResolver.setToken(token);
//        GoodReceiveNote goodReceiveNote = new GoodReceiveNote();
//        goodReceiveNote.setGrnNo(goodReceiveNote.getGrnNo());
//        goodReceiveNote.setGrnStatus(GrnStatus.PENDING_QC);
//        if(token != null)
//            goodReceiveNote.setCreatedBy(claimResolver.getEmployee().get());
//
//        goodReceiveNote.setGoodReceiveItemDetails(
//                goodReceiveNoteDto.getGoodReceiveItemDetails().stream().map(
//                        goodReceiveItemDetailDto -> {
//                            goodReceiveNote.setWarehouse(new Warehouse(goodReceiveItemDetailDto.getWarehouse().getId()));
//
//                            GoodReceiveItemDetail goodReceiveItemDetail = new GoodReceiveItemDetail();
//                            goodReceiveItemDetail.setItem(new Item(goodReceiveItemDetailDto.getItem().getId()));
//                            goodReceiveItemDetail.setCategory(new ItemCategory(goodReceiveItemDetailDto.getCategory().getId()));
//                            goodReceiveItemDetail.setSubCategory(new ItemCategory(goodReceiveItemDetailDto.getSubCategory().getId()));
//                            goodReceiveItemDetail.setWarehouse(new Warehouse(goodReceiveItemDetailDto.getWarehouse().getId()));
//                            if(goodReceiveItemDetailDto.getWarehouseStore()!=null){
//                                goodReceiveItemDetail.setWarehouseStore(new WarehouseStore(goodReceiveItemDetailDto.getWarehouseStore().getId()));
//                            }
//                            goodReceiveItemDetail.setReceiveQty(BigDecimal.valueOf(goodReceiveItemDetailDto.getOrderQty()));
//                            if(goodReceiveItemDetailDto.getManufactureDate()!=null){
//                                goodReceiveItemDetail.setManufactureDate(goodReceiveItemDetailDto.getManufactureDate());
//                            }
//                            if(goodReceiveItemDetailDto.getExpireDate()!=null){
//                                goodReceiveItemDetail.setExpireDate(goodReceiveItemDetailDto.getExpireDate());
//                            }
//                            goodReceiveItemDetail.setGoodReceiveNote(goodReceiveNote);
//                            return goodReceiveItemDetail;
//                        }
//                ).collect(Collectors.toList())
//        );
//        grnRepository.save(goodReceiveNote);
//    }

    @Override
    @Transactional
    public void createManualGrn(Jwt token, GrnManualRequestDto grnManualDto, GrnMode mode) {
        claimResolver.setToken(token);
        String uri = "";
        if(claimResolver.getEmployee()==null){
            throw new RuntimeException("Sorry! Employee profile required");
        }
        GoodReceiveNote grn = new GoodReceiveNote();
        grn.setGrnDate(LocalDate.now());
        if(mode.equals(GrnMode.AUTO)) {
            grn.setRemotePoId(grnManualDto.getPoId());
            grn.setGrnStatus(GrnStatus.PENDING);
        }
        grn.setGrnNo(grnManualDto.getGrnNo());
        grn.setIndentNo(grnManualDto.getIndentNo() );
        grn.setGrnMode(mode);
        if(mode.equals(GrnMode.MANUAL)) {
            grn.setGrnStatus(GrnStatus.PENDING_QC);
        }

        grn.setWarehouse(new Warehouse(grnManualDto.getWarehouseId()));
        if(token != null && claimResolver.getEmployee().isPresent()){
            grn.setCreatedBy(claimResolver.getEmployee().get());
        }


        grn.setGoodReceiveItemDetails(grnManualDto.getGrnDetails().stream().map(detailDto->{
            GoodReceiveItemDetail grid = new GoodReceiveItemDetail();

                Optional<Item> itemOp = Optional.empty();
                if(mode.equals(GrnMode.MANUAL)) {
                    itemOp = itemService.getItemDetail(detailDto.getItem().getId());
                }else if (mode.equals(GrnMode.AUTO)){
                    List<Item> items = itemService.getByCode(detailDto.getItemCode());
                    List<Long> itemIds = items.stream().map(Item::getId).toList();
                    List<ItemStock> stocks = itemService.getByItemAndWarehouse(itemIds,grnManualDto.getWarehouseId());
                    if(!stocks.isEmpty()){
                       ItemStock stock = stocks.get(0);
                        itemOp = Optional.of(stock.getItem());
                    }
                }
                if(itemOp.isPresent()){
                    Item item = itemOp.get();
                    grid.setItem(item);
                    grid.setBrandName(item.getName());
                    grid.setCategory(item.getItemParentCategory());
                    grid.setSubCategory(item.getItemCategory());
                    grid.setEstimatedDeliveryDays(detailDto.getEstDeliveryDays());
                    grid.setReceiveQty(detailDto.getOrderQty());
                    grid.setPricePerUnit(detailDto.getPricePerUnit());
                    grid.setDeliveryCharge(detailDto.getDeliveryChargeAmount());
                    grid.setWarehouse(new Warehouse(grnManualDto.getWarehouseId()));
                    grid.setGoodReceiveNote(grn);
                }

                return grid;
            }).collect(Collectors.toList())
        );

        grn.setAitOption(grnManualDto.getAitOption());
        grn.setVatOption(grnManualDto.getVatOption());
        grn.setVat(grnManualDto.getTotalVat());
        grn.setVatPctg(grnManualDto.getVatPctg());
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

//        Optional<GoodReceiveNote> qcOp = (Optional<GoodReceiveNote>) qcService.getByGrnId(grn.getId());

        qcMailService.setClaimResolver(claimResolver);
        qcMailService.setQualityControl(grn);
        qcMailService.getAuthorizedUsers(uri);
        qcMailService.sentMail(null,"Pending Demand");

    }

    @Override
    public void createAutoGrn(Jwt token, GrnManualRequestDto grnManualDto) {
        grnManualDto.setGrnNo(getNextGrnNumber());
        createManualGrn(token,grnManualDto,GrnMode.AUTO);
    }

    @Override
    public Page<?> getAllGrn(Jwt token, Optional<Integer> page,
                             Optional<Integer> size, Optional<String> grnNo,
                             Optional<Integer> qty,Optional<Integer> receivedQty,
                             Optional<String> fromDate, Optional<String> toDate, Optional<String> grnStatus) {
        claimResolver.setToken(token);
        String uri = "inventory-management/good-receive/good-receive-note";
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        List<Long> categoryIds = dataFilter.getCategoryIds();

        if(fromDate.isPresent() && toDate.isPresent()){
            fromDateObj = LocalDateTime.parse(fromDate.get()+"T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get()+"T23:59:59");
        }
        return grnRepository.findAllGrn(pageable,
                warehouseIds,categoryIds,
                grnNo.orElse(null),
                qty.orElse(null), receivedQty.orElse(null),
                fromDateObj,toDateObj,grnStatus.orElse(null));
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
                grnDetailInfo.setDeclineNote(detailInfo.getDeclineNote());
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
                    grnidi.setApproveComment(goodReceiveNoteItemDetailInfo.getApproveComment());
                    grnidi.setDeclineComment(goodReceiveNoteItemDetailInfo.getDeclineComment());
                    grnidi.setCreatedAt(goodReceiveNoteItemDetailInfo.getCreatedAt());
                    grnidi.setId(goodReceiveNoteItemDetailInfo.getId());
                    grnidi.setReceiveQty(goodReceiveNoteItemDetailInfo.getReceiveQty());
                    grnidi.setDeclaredQty(goodReceiveNoteItemDetailInfo.getDeclaredQty());
                    grnidi.setInspectedQty(goodReceiveNoteItemDetailInfo.getInspectedQty());
                    grnidi.setTotalApprovedQty(goodReceiveNoteItemDetailInfo.getTotalApprovedQty());
                    grnidi.setTotalDeclinedQty(goodReceiveNoteItemDetailInfo.getTotalDeclinedQty());
                    grnidi.setExpireDate(goodReceiveNoteItemDetailInfo.getExpireDate());
                    grnidi.setManufactureDate(goodReceiveNoteItemDetailInfo.getManufactureDate());
                    grnidi.setItem(itemService.getItemDetailWithWarehouse(goodReceiveNoteItemDetailInfo.getItem().getId()));
                    detailInfos.add(grnidi);
                });
                grnDetailInfo.setGoodReceiveItemDetails(detailInfos);
                List<QcQuery.QcResultItem> qcResultByGrn = (List<QcQuery.QcResultItem>)qcService.getQcResultByGrn(detailInfo.getId());

                _detailInfo.put("detailInfo", grnDetailInfo);
                _detailInfo.put("qcComment", ((!qcResultByGrn.isEmpty()) ? qcResultByGrn.get(0).getComment():""));
                _detailInfo.put("qcId", ((!qcResultByGrn.isEmpty()) ? qcResultByGrn.get(0).getId():null));
                _detailInfo.put("reviewerId", ((!qcResultByGrn.isEmpty()) ? qcResultByGrn.get(0).getReviewerId():null));
                _detailInfo.put("qcStatus", ((!qcResultByGrn.isEmpty()) ? qcResultByGrn.get(0).getQcStatus():""));
                _detailInfo.put("prevStatus", ((!qcResultByGrn.isEmpty()) ? qcResultByGrn.get(0).getPrevStatus():""));
                _detailInfo.put("qcResult", qcResultByGrn);
            }
                return Optional.ofNullable(_detailInfo);
            }


    }



    @Override
    public Page<?> getAllGrnPendingQC(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                      Optional<String> grnNo, Optional<Integer> qty, Optional<Integer> receivedQty,
                                      Optional<String> fromDate, Optional<String> toDate) {
        claimResolver.setToken(token);
        String uri="inventory-management/good-receive/quality-check";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds= dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        List<Long> categoryIds = dataFilter.getCategoryIds();

        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + "T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get() + "T23:59:59");
        }
        List<String> status = new ArrayList<>();
        status.add(GrnStatus.PENDING_QC.toString());
        status.add(GrnStatus.QC_PARTIAL.toString());
        status.add(GrnStatus.QC_PASS.toString());
        status.add(GrnStatus.QC_FAILED.toString());
        return grnRepository.findAllGrnByStatus(
                null,null,
                grnNo.orElse(null), qty.orElse(null), receivedQty.orElse(null),
                status,
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

    @Override
    @Transactional
    public void receivedPO(Jwt token,Long id) {
        Optional<GoodReceiveNote> grnOp = grnRepository.findById(id);
        if(grnOp.isPresent()){
            GoodReceiveNote grn = grnOp.get();
            grn.setGrnStatus(GrnStatus.PENDING_QC);
            Long remotePoId = grn.getRemotePoId();
            sentGrnReceived(token,remotePoId,GrnStatus.RECEIVED,new NoteDto());
        }
    }

    @Override
    @Transactional
    public void declinePO(Jwt token,Long id, NoteDto noteDto) {
        Optional<GoodReceiveNote> grnOp = grnRepository.findById(id);
        if(grnOp.isPresent()){
            GoodReceiveNote grn = grnOp.get();
            grn.setGrnStatus(GrnStatus.REJECTED);
            Long remotePoId = grn.getRemotePoId();
            sentGrnReceived(token,remotePoId,GrnStatus.REJECTED,noteDto);
        }
    }

    @Transactional
    private void sentGrnReceived(Jwt token,Long id,GrnStatus status, NoteDto noteDto){
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
        if(orgOp.isPresent()){
            headers.set("orgId",orgOp.get().getCpsVendorRegistrationId().toString());
        }

        HttpEntity<NoteDto> payload = new HttpEntity<>(noteDto,headers);
        String url = (status.equals(GrnStatus.RECEIVED))? cpsConfig.getPoReceiveEndpoint(id): cpsConfig.getPoRejectEndpoint(id);
        System.out.println(url);
        ResponseEntity<?> response = networkService.put(url, payload, Void.class);
        if(response.getStatusCode()!= HttpStatus.NO_CONTENT){
            throw new RuntimeException("Sorry! Something wrong");
        }

    }
}
