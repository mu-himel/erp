package com.agi.aesl.erpscm.goods_receive.service;

import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.goods_receive.dto.request.GrnManualItemDetailDto;
import com.agi.aesl.erpscm.goods_receive.dto.request.GrnManualRequestDto;
import com.agi.aesl.erpscm.goods_receive.dto.response.GoodReceiveNoteItemDetailInfo;
import com.agi.aesl.erpscm.goods_receive.dto.response.GrnDetailInfo;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnMode;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.repository.GrnDetailRepository;
import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository;
import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository.GoodReceiveNoteDetailInfo;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.quality_control.repository.QcQuery;
import com.agi.aesl.erpscm.quality_control.service.QcMailService;
import com.agi.aesl.erpscm.quality_control.service.QcService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.RequiredArgsConstructor;
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

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GrnServiceImpl implements GrnService{


    private final GrnRepository grnRepository;


    private final ClaimResolver claimResolver;


    private final IntegrationReaderService integrationReaderService;


    private final ItemService itemService;


    private final QcService qcService;


    private final GrnDetailRepository grnDetailRepository;


    private final QcMailService qcMailService;


    private final OrgService orgService;


    private final CpsServerConfig cpsConfig;


    private final NetworkService networkService;
    private CreateGrnService createGrnService;

    private Employee getEmp(){
        return claimResolver.getEmployee().orElse(null);
    }

    private void setCreateServiceDependencies(CreateGrnService createGrnService){
        createGrnService.setClaimResolver(claimResolver);
        createGrnService.setItemService(itemService);
        createGrnService.setCreatedBy(getEmp());
        createGrnService.setGrnRepository(grnRepository);
        createGrnService.setQcMailService(qcMailService);
    }

    @Override
    @Transactional
    public String createManualGrn(Jwt token, GrnManualRequestDto grnManualDto, GrnMode mode) {
        setCreateServiceDependencies(createGrnService);
        return createGrnService.createGrn(token,grnManualDto,mode);
    }

    @Override
    @Transactional
    public void createAutoGrn(Jwt token, GrnManualRequestDto grnManualDto) {
        setCreateServiceDependencies(createGrnService);
        createGrnService.createGrn(token,grnManualDto,GrnMode.AUTO);
    }

    @Override
    public Page<GrnRepository.GoodReceiveNoteInfo> getAllGrn(Jwt token, Optional<Integer> page,
                                                             Optional<Integer> size, Optional<String> grnNo,
                                                             Optional<Integer> qty, Optional<Integer> receivedQty,
                                                             Optional<String> fromDate, Optional<String> toDate, Optional<String> grnStatus) {
        claimResolver.setToken(token);
        if(claimResolver.getEmployee().isEmpty()){
            throw new AesException("Sorry! Store Profile Required");
        }
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
        claimResolver.getEmployee().ifPresent(employee->warehouseIds.add(employee.getWarehouseId()));
        return grnRepository.findAllGrn(pageable,
                warehouseIds,categoryIds,
                grnNo.orElse(null),
                qty.orElse(null), receivedQty.orElse(null),
                fromDateObj,toDateObj,grnStatus.orElse(null));
    }

    @Override
    public <T> T getGrnById(Long id, Boolean returnTypeEntity) {
        if(Boolean.TRUE.equals(returnTypeEntity)){
            return (T) grnRepository.findById(id);
        }else{
            Optional<GoodReceiveNoteDetailInfo> goodReceiveItemDetailOp = grnRepository.findGrnById(id);
            Map<String, Object> detailInfoMap = new HashMap<>();
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

                List<GoodReceiveNoteItemDetailInfo> detailInfos = new ArrayList<>();
                detailInfo.getGoodReceiveItemDetails().forEach(goodReceiveNoteItemDetailInfo -> {
                    GoodReceiveNoteItemDetailInfo grnidi = new GoodReceiveNoteItemDetailInfo();

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
                List<QcQuery.QcResultItem> qcResultByGrn = qcService.getQcResultByGrn(detailInfo.getId());

                detailInfoMap.put("detailInfo", grnDetailInfo);
                updateGrnDetail(detailInfoMap,qcResultByGrn);

            }
                return (T) Optional.of(detailInfoMap);
            }


    }

    private void updateGrnDetail(Map<String,Object> detailInfoMap,List<QcQuery.QcResultItem> qcResultByGrn){
        detailInfoMap.put("qcComment", ((!qcResultByGrn.isEmpty()) ? qcResultByGrn.get(0).getComment():""));
        detailInfoMap.put("qcId", ((!qcResultByGrn.isEmpty()) ? qcResultByGrn.get(0).getId():null));
        detailInfoMap.put("reviewerId", ((!qcResultByGrn.isEmpty()) ? qcResultByGrn.get(0).getReviewerId():null));
        detailInfoMap.put("qcStatus", ((!qcResultByGrn.isEmpty()) ? qcResultByGrn.get(0).getQcStatus():""));
        detailInfoMap.put("prevStatus", ((!qcResultByGrn.isEmpty()) ? qcResultByGrn.get(0).getPrevStatus():""));
        detailInfoMap.put("qcResult", qcResultByGrn);
    }

    @Override
    public Page<GrnRepository.GoodReceiveNoteInfo> getAllGrnPendingQC(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                      Optional<String> grnNo, Optional<Integer> qty, Optional<String> grnMode,
                                      Optional<String> poNo,
                                      Optional<Integer> receivedQty,
                                      Optional<String> fromDate, Optional<String> toDate) {
        claimResolver.setToken(token);
        if(claimResolver.getEmployee().isEmpty()){
            throw new AesException("Sorry! Qc Relevant Employee Profile Required");
        }
        String uri="inventory-management/good-receive/quality-check";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds= dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        List<Long> categoryIds = dataFilter.getCategoryIds();
        claimResolver.getEmployee().ifPresent(employee->warehouseIds.add(employee.getWarehouseId()));
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
                warehouseIds,categoryIds,
                grnNo.orElse(null), qty.orElse(null), grnMode.orElse(null),
                poNo.orElse(null), receivedQty.orElse(null),
                status,
                fromDateObj,
                toDateObj,pageable);
    }

    @Override
    public void updateGrnItemDetail(GoodReceiveItemDetail goodReceiveItemDetail) {
        grnDetailRepository.save(goodReceiveItemDetail);
    }

    @Override
    public <T> T getGrn(Long id, boolean returnTypeEntity) {
        if(returnTypeEntity){
            //noinspection unchecked
            return (T) grnRepository.findById(id);
        }else{

            Optional<GrnRepository.GoodReceiveNoteDetailInfo> goodReceiveItemDetailOp = grnRepository.findGrnById(id);
            Map<String, Object> detailInfoMap = new HashMap<>();
            if(goodReceiveItemDetailOp.isPresent()) {
                GrnRepository.GoodReceiveNoteDetailInfo detailInfo =  goodReceiveItemDetailOp.get();

                detailInfoMap.put("detailInfo",detailInfo);
                detailInfoMap.put("qcResult",qcService.getQcResultByGrn(detailInfo.getId()));

            }
            //noinspection unchecked
            return (T) Optional.of(detailInfoMap);
        }
    }

    @Override
    public Optional<GoodReceiveNote> getByGrnNo(String grnNo) {
        return grnRepository.findByGrnNo(grnNo);
    }

    @Override
    @Transactional
    public void receivedPO(Jwt token, Long id, List<GrnManualItemDetailDto> goodReceiveItemDetailDtos) {
        Optional<GoodReceiveNote> grnOp = grnRepository.findById(id);
        GoodReceiveNote grn = null;
        for(GrnManualItemDetailDto gmidto: goodReceiveItemDetailDtos) {
            Optional<GoodReceiveItemDetail> grnDetailOp = grnDetailRepository.findById(gmidto.getId());

            if (grnOp.isPresent()) {
                grn = grnOp.get();
                if (grnDetailOp.isPresent()) {
                    GoodReceiveItemDetail grnd = grnDetailOp.get();
                    grnd.setExpireDate(gmidto.getExpireDate());
                    grnd.setManufactureDate(gmidto.getProductionDate());
                }
                grn.setGrnStatus(GrnStatus.PENDING_QC);


            }
        }
        if(grn!=null && grn.getGrnMode().equals(GrnMode.AUTO)) {
                sentGrnReceived(token, grn.getPoNo(), GrnStatus.RECEIVED, new NoteDto());
            }

    }

    @Override
    @Transactional
    public void declinePO(Jwt token,Long id, NoteDto noteDto) {
        Optional<GoodReceiveNote> grnOp = grnRepository.findById(id);
        if(grnOp.isPresent()){
            GoodReceiveNote grn = grnOp.get();
            grn.setGrnStatus(GrnStatus.REJECTED);
            if(grn.getGrnMode().equals(GrnMode.AUTO)) {
                sentGrnReceived(token, grn.getPoNo(), GrnStatus.REJECTED, noteDto);
            }
        }
    }

    public void sentGrnReceived(Jwt token,String id,GrnStatus status, NoteDto noteDto){
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
        orgOp.ifPresent(organization -> headers.set("orgId", organization.getCpsVendorRegistrationId().toString()));

        HttpEntity<NoteDto> payload = new HttpEntity<>(noteDto,headers);
        String url = (status.equals(GrnStatus.RECEIVED))? cpsConfig.getPoReceiveEndpoint(id): cpsConfig.getPoRejectEndpoint(id);

        ResponseEntity<?> response = networkService.put(url, payload, Void.class);
        if(response.getStatusCode()!= HttpStatus.NO_CONTENT){
            throw new AesException("Sorry! Something wrong");
        }

    }
}
