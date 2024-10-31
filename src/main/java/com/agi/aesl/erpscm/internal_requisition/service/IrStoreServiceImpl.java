package com.agi.aesl.erpscm.internal_requisition.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.internal_requisition.dto.request.ReceiveStockDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.StoreIRReqDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.TransferStockDto;
import com.agi.aesl.erpscm.internal_requisition.entity.InternalRequisition;
import com.agi.aesl.erpscm.internal_requisition.entity.InternalRequisitionDetailWarehouse;
import com.agi.aesl.erpscm.internal_requisition.entity.StoreIR;
import com.agi.aesl.erpscm.internal_requisition.entity.StoreIRVAHistory;
import com.agi.aesl.erpscm.internal_requisition.enums.IrStatus;
import com.agi.aesl.erpscm.internal_requisition.repository.InternalRequisitionRepository;
import com.agi.aesl.erpscm.internal_requisition.repository.IrDetailWarehouseRepository;
import com.agi.aesl.erpscm.internal_requisition.repository.StoreIrRepository;
import com.agi.aesl.erpscm.internal_requisition.repository.StoreIrVAHistoryRepository;
import com.agi.aesl.erpscm.inventory.dto.response.ItemDetail;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class IrStoreServiceImpl implements IrStoreService{

    private static final Integer PAGE_SIZE = 20;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private StoreIrRepository storeIrRepository;

    @Autowired
    private InternalRequisitionRepository irRepository;

    @Autowired
    private StoreIrVAHistoryRepository storeIrVAHistoryRepository;

    @Autowired
    private IrDetailWarehouseRepository irDetailWarehouseRepository;

    @Autowired
    private UserApplicationValidatorService<StoreIR> verificationService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private CommentService commentService;


    @Override
    @Transactional
    public void submit(Jwt token, String uri, StoreIRReqDto storeIRReqDto) {
        Optional<StoreIR> sIrOp = storeIrRepository.findById(storeIRReqDto.getId());

        StoreIR sIR = sIrOp.get();
        InternalRequisition ir = sIR.getIr();
        List<String> cateIds = new ArrayList<>();
        cateIds.add(ir.getCategory().getId().toString());

        ir.getDetails().stream().forEach(ird->{
            cateIds.add(ird.getSubCategory().getId().toString());
        });

        AppliedVADto appliedVADto = verificationService.applyVerifyApprovalProcess(sIR, DomainType.PSIR,
                IrStatus.APPROVED.toString(), uri, "CATEGORY", cateIds, null);

        if(appliedVADto.getPanels().isEmpty() && appliedVADto.getVerifiers().isEmpty()){
            sIR.setIrStatus(IrStatus.COMPLETED);
        }
//        var verifierOp =  verificationService.getVerifiers(loggedInUser, uri, categories);
//        if(verifierOp instanceof Optional){
//            List<Verifier> verifiers = new ArrayList<>();
//            if (verifierOp.isPresent()) {
//
//                var verification =  (Map<String,Object>)verifierOp.get();
//
//                verifiers = (List<Verifier>) verification.get("verifiers");
//
//
//                Boolean verificationRequired = (Boolean) verification.get("verificationRequired");
//                if (verificationRequired != null && verificationRequired == true && verifiers != null && verifiers.size() > 0) {
//                    sIR.setStatus(IrStatus.PENDING_VERIFICATION);
//                } else {
//                    sIR.setStatus(IrStatus.PENDING);
//                }
//
//            } else {
//                sIR.setStatus(IrStatus.PENDING);
//            }
//
//            verificationService.setVerifiers(sIR, verifiers, DomainType.PSIR);
//
//            List<ApprovalSettingQuery.ApprovalPanel> approvalPanels = approvalSettingService
//                    .getModuleWiseApprovalSetting(uri,
//                            Optional.ofNullable(categories),Optional.empty());
//
//            verificationService.setApprovers(sIR, approvalPanels, DomainType.PSIR);
//        }
    }

    @Override
    public Optional<?> getStoreIRDetail(Long id) {
        Optional<StoreIR> sIROp = storeIrRepository.findById(id);
        if(sIROp.isEmpty()){
            throw new RuntimeException("Sorry! Store IR not found");
        }

        StoreIR storeIR = sIROp.get();


        var irOp = irRepository.findById(storeIR.getIr().getId(), InternalRequisitionRepository.IrDetail.class);
        Map<String,Object> detailMap = new HashMap<>();
        if(irOp instanceof Optional){
            InternalRequisitionRepository.IrDetail irDetail = irOp.get();
            detailMap.put("priority",irDetail.getPriority());
            detailMap.put("id",storeIR.getId());
            detailMap.put("internalRequisitionNo",irDetail.getInternalRequisitionNo());
            detailMap.put("category",irDetail.getCategory());
            detailMap.put("details",irDetail.getDetails());
            detailMap.put("deliveryDate",irDetail.getDeliveryDate());
            detailMap.put("requestedBy",storeIR.getRequestedBy());
            detailMap.put("warehouse",irDetail.getWarehouse());

            List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                    .getVerificationsByDomainTypeAndDomainId(DomainType.PSIR, storeIR.getId());
            vrs.stream().forEach(verifier->{
                if(verifier.getIsApproval()==false){
                    verifiers.add(verifier);
                }else{
                    approvers.add(verifier);
                }
            });

            List<?> comments = commentService.getCommentsByDomain(DomainType.PSIR, storeIR.getId());
            detailMap.put("comments",comments);
            detailMap.put("verifiers", verifiers);
            detailMap.put("approvers",approvers);
            detailMap.put("status",storeIR.getStatus());
        }
        return Optional.ofNullable(detailMap);
    }

    @Override
    public Page<?> getPendingStoreIrs(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                      Optional<String> fromDateStr, Optional<String> toDateStr) {
        claimResolver.setToken(token);
        Optional<Employee> empOp = claimResolver.getEmployee();
        if(empOp.isEmpty()){
            throw new RuntimeException("Sorry! Employee Profile required");
        }
        Employee employee = empOp.get();
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateStr.isPresent()){
            fromDate = LocalDateTime.parse(fromDateStr.get()+"T00:00:00");
        }
        if(toDateStr.isPresent()){
            toDate = LocalDateTime.parse(toDateStr.get()+"T23:59:59");
        }
        return storeIrRepository.findAllPendingIrs(employee.getWarehouseId(),fromDate, toDate,pageable);
    }

    @Override
    @Transactional
    public void transferStock(TransferStockDto transferStockDto) {
        Optional<InternalRequisitionDetailWarehouse> irdwOp =  irDetailWarehouseRepository.findById(transferStockDto.getId());
        if(irdwOp.isPresent()){
            InternalRequisitionDetailWarehouse irdw = irdwOp.get();
            irdw.setTransferQty(transferStockDto.getTransferQty());
            irdw.setInTransit(transferStockDto.getTransferQty());
            irdw.setIsDecline(null);
            Optional<Item> itemOp = itemService.getItemDetail(transferStockDto.getItemId());
            if(itemOp.isPresent()){
                Item item = itemOp.get();
                var itemDetailOp = itemService.getItemDetailWithWarehouse(item.getId());
                if(itemDetailOp instanceof Optional && itemDetailOp.isPresent()){
                    ItemDetail itemDetail = (ItemDetail) itemDetailOp.get();
                    List<Map<String,Object>> warehouses = (List<Map<String,Object>>)itemDetail
                            .getWarehouses()
                            .get(transferStockDto.getWarehouseId().toString());
                    if(warehouses.size()>0){
                        Map<String,Object> warehouseStoreInfo = warehouses.get(0);

                        itemService.stockOut(new Item(itemDetail.getId()), transferStockDto.getTransferQty(),
                                (Long)warehouseStoreInfo.get("warehouseId"),
                                (Long)warehouseStoreInfo.get("warehouseStoreId"));
                    }

                }

            }

        }
    }

    @Override
    @Transactional
    public void receiveStock(ReceiveStockDto receiveStockDto) {
        Optional<InternalRequisitionDetailWarehouse> irdwOp = irDetailWarehouseRepository.findById(receiveStockDto.getId());
        if(irdwOp.isPresent()){
            InternalRequisitionDetailWarehouse irdw = irdwOp.get();
            irdw.setReceivedQty(irdw.getTransferQty());
            irdw.setInTransit(null);
            irdw.setReceiveNote(receiveStockDto.getNote());
            Optional<Item> itemOp = itemService.getItemDetail(receiveStockDto.getItemId());
            if(itemOp.isPresent()){
                Item item = itemOp.get();
                var itemDetailOp = itemService.getItemDetailWithWarehouse(item.getId());
                if(itemDetailOp instanceof Optional && itemDetailOp.isPresent()){
                    ItemDetail itemDetail = (ItemDetail) itemDetailOp.get();
                    List<Map<String,Object>> warehouses = (List<Map<String,Object>>)itemDetail
                            .getWarehouses()
                            .get(receiveStockDto.getWarehouseId().toString());
                    if(warehouses.size()>0){
                        Map<String,Object> warehouseStoreInfo = warehouses.get(0);
                        itemService.stockIn(new Item(itemDetail.getId()), irdw.getReceivedQty(),
                                (Long)warehouseStoreInfo.get("warehouseId"),
                                (Long)warehouseStoreInfo.get("warehouseStoreId"));
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void declineStock(ReceiveStockDto receiveStockDto) {
        Optional<InternalRequisitionDetailWarehouse> irdwOp = irDetailWarehouseRepository.findById(receiveStockDto.getId());
        if(irdwOp.isPresent()){
            InternalRequisitionDetailWarehouse irdw = irdwOp.get();
            irdw.setIsDecline(true);
            irdw.setInTransitReturn(irdw.getInTransit());
            irdw.setInTransit(null);
            irdw.setDeclineNote(receiveStockDto.getNote());

        }
    }

    @Override
    public Page<?> getReadyForTransfer(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return storeIrRepository.findAllReadyForTransfer(claimResolver.getEmployee().get().getWarehouseId(),pageable);
    }

    @Override
    public Page<?> getPendingStoreIrVerification(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return storeIrRepository.findAllPendingIrsVerification(claimResolver.getEmployee().get().getWarehouseId(),
                claimResolver.getUserId(),
                pageable);
    }

    @Override
    public Page<?> getPendingStoreIrApproval(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return storeIrRepository.findAllPendingIrsApproval(claimResolver.getEmployee().get().getWarehouseId()
                ,claimResolver.getUserId(),
                pageable);
    }

    @Override
    public Page<?> getReceiveStoreRequisitions(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return storeIrRepository.findAllReceiveRequisitions(claimResolver.getEmployee().get().getWarehouseId(),
                pageable);
    }

    @Override
    @Transactional
    public void acceptReturn(ReceiveStockDto receiveStockDto) {
        Optional<InternalRequisitionDetailWarehouse> irdwOp = irDetailWarehouseRepository.findById(receiveStockDto.getId());
        if(irdwOp.isPresent()){
            InternalRequisitionDetailWarehouse irdw = irdwOp.get();
            irdw.setIsDecline(null);
            irdw.setTransferQty(null);
            irdw.setInTransit(null);
            irdw.setDeclineNote(null);
            Optional<Item> itemOp = itemService.getItemDetail(receiveStockDto.getItemId());
            if(itemOp.isPresent()){
                Item item = itemOp.get();
                var itemDetailOp = itemService.getItemDetailWithWarehouse(item.getId());
                if(itemDetailOp instanceof Optional && itemDetailOp.isPresent()){
                    ItemDetail itemDetail = (ItemDetail) itemDetailOp.get();
                    List<Map<String,Object>> warehouses = (List<Map<String,Object>>)itemDetail
                            .getWarehouses()
                            .get(receiveStockDto.getWarehouseId().toString());
                    if(warehouses.size()>0){
                        Map<String,Object> warehouseStoreInfo = warehouses.get(0);
                        itemService.stockIn(new Item(itemDetail.getId()), irdw.getInTransitReturn(),
                                (Long)warehouseStoreInfo.get("warehouseId"),
                                (Long)warehouseStoreInfo.get("warehouseStoreId"));
                    }
                }
            }
            irdw.setInTransitReturn(null);
        }
    }

    @Transactional
    private void saveHistory(StoreIR sIR, String id, IrStatus status){
        StoreIRVAHistory irVAHistory = new StoreIRVAHistory();
        irVAHistory.setStoreIr(sIR);
        irVAHistory.setEmployee(new Employee(id));
        irVAHistory.setIrStatus(status);
        storeIrVAHistoryRepository.save(irVAHistory);
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<StoreIR> sIrOptional  = storeIrRepository.findById(id);
        if(sIrOptional.isPresent()){
            StoreIR sIR = sIrOptional.get();
            saveHistory(sIR, verification.getVerifier().getId(), IrStatus.VERIFIED);
            sIR.setNextVerifierId(nextVerifier.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<StoreIR> sIROptional  = storeIrRepository.findById(id);
        if(sIROptional.isPresent()){
            StoreIR sIR = sIROptional.get();
            saveHistory(sIR, verification.getVerifier().getId(), IrStatus.APPROVED);
            sIR.setNextApproverId(nextApprover.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<StoreIR> sIROptional  = storeIrRepository.findById(id);
        if(sIROptional.isPresent()){
            StoreIR sIR = sIROptional.get();
            if(firstApprover.isPresent()){
                sIR.setNextApproverId(firstApprover.get().getVerifier().getId());
                sIR.setIrStatus(IrStatus.PENDING_APPROVAL);
            }else {
                sIR.setIrStatus(IrStatus.VERIFIED);
            }
            saveHistory(sIR, sIR.getNextVerifierId(),IrStatus.VERIFIED);

        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<StoreIR> irOptional  = storeIrRepository.findById(id);
        if(irOptional.isPresent()){
            StoreIR sIR = irOptional.get();
            sIR.setIrStatus(IrStatus.APPROVED);
            saveHistory(sIR, sIR.getNextApproverId(), IrStatus.APPROVED);
        }
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<StoreIR> sIROptional  = storeIrRepository.findById(domainId);
        if(sIROptional.isPresent()){
            StoreIR sIR = sIROptional.get();
            sIR.setReviewerId(reviewer.getId());
            sIR.setIrStatus(IrStatus.REVIEW);
            sIR.setReviewDate(LocalDateTime.now());
        }
    }

    @Override
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {

    }
}
