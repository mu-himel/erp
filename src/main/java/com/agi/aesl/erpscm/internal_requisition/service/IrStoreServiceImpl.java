package com.agi.aesl.erpscm.internal_requisition.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.exception.AesException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IrStoreServiceImpl implements IrStoreService{

    private static final Integer PAGE_SIZE = 20;


    private final ClaimResolver claimResolver;


    private final StoreIrRepository storeIrRepository;


    private final InternalRequisitionRepository irRepository;


    private final StoreIrVAHistoryRepository storeIrVAHistoryRepository;


    private final IrDetailWarehouseRepository irDetailWarehouseRepository;


    private final UserApplicationValidatorService<StoreIR> verificationService;


    private final ItemService itemService;


    private final CommentService commentService;

    private static final String DATE_TIME_START="T00:00:00";
    private static final String DATE_TIME_END="T23:59:59";
    private static final String WAREHOUSE_ID_KEY="warehouseId";
    private static final String WAREHOUSE_STORE_ID_KEY="warehouseStoreId";

    private Long getEmpWarehouseId(){
        Employee emp = claimResolver.getEmployee().orElse(null);
        return emp!=null? emp.getWarehouseId() : null;
    }

    @Override
    @Transactional
    public void submit(Jwt token, String uri, StoreIRReqDto storeIRReqDto) {
        Optional<StoreIR> sIrOp = storeIrRepository.findById(storeIRReqDto.getId());
        if(sIrOp.isPresent()) {
            StoreIR sIR = sIrOp.get();
            InternalRequisition ir = sIR.getIr();
            List<String> cateIds = new ArrayList<>();
            cateIds.add(ir.getCategory().getId().toString());

            ir.getDetails().forEach(ird ->
                    cateIds.add(ird.getSubCategory().getId().toString())
            );

            AppliedVADto appliedVADto = verificationService.applyVerifyApprovalProcess(sIR, DomainType.PSIR,
                    IrStatus.APPROVED.toString(), uri, "CATEGORY", cateIds, null);

            if (appliedVADto.getPanels().isEmpty() && appliedVADto.getVerifiers().isEmpty()) {
                sIR.setIrStatus(IrStatus.COMPLETED);
            }
        }
    }

    @Override
    public Optional<Map<String,Object>> getStoreIRDetail(Long id) {
        Optional<StoreIR> sIROp = storeIrRepository.findById(id);
        if(sIROp.isEmpty()){
            throw new AesException("Sorry! Store IR not found");
        }

        StoreIR storeIR = sIROp.get();


        var irOp = irRepository.findById(storeIR.getIr().getId(), InternalRequisitionRepository.IrDetail.class);
        Map<String,Object> detailMap = new HashMap<>();
        if(irOp instanceof Optional && irOp.isPresent()){
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
            vrs.forEach(verifier->{
                if(Boolean.FALSE.equals(verifier.getIsApproval())){
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
        return Optional.of(detailMap);
    }

    @Override
    public Page<StoreIrRepository.PendingStoreIR> getPendingStoreIrs(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                                     Optional<String> fromDateStr, Optional<String> toDateStr) {
        claimResolver.setToken(token);
        Optional<Employee> empOp = claimResolver.getEmployee();
        if(empOp.isEmpty()){
            throw new AesException("Sorry! Employee Profile required");
        }
        Employee employee = empOp.get();
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateStr.isPresent()){
            fromDate = LocalDateTime.parse(fromDateStr.get()+DATE_TIME_START);
        }
        if(toDateStr.isPresent()){
            toDate = LocalDateTime.parse(toDateStr.get()+DATE_TIME_END);
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
                    List<Map<String,Object>> warehouses = itemDetail
                            .getWarehouses()
                            .get(transferStockDto.getWarehouseId().toString());
                    if(!warehouses.isEmpty()){
                        Map<String,Object> warehouseStoreInfo = warehouses.get(0);

                        itemService.stockOut(new Item(itemDetail.getId()), transferStockDto.getTransferQty(),
                                (Long)warehouseStoreInfo.get(WAREHOUSE_ID_KEY),
                                (Long)warehouseStoreInfo.get(WAREHOUSE_STORE_ID_KEY));
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
                    List<Map<String,Object>> warehouses = itemDetail
                            .getWarehouses()
                            .get(receiveStockDto.getWarehouseId().toString());
                    if(!warehouses.isEmpty()){
                        Map<String,Object> warehouseStoreInfo = warehouses.get(0);
                        itemService.stockIn(new Item(itemDetail.getId()), irdw.getReceivedQty(),
                                (Long)warehouseStoreInfo.get(WAREHOUSE_ID_KEY),
                                (Long)warehouseStoreInfo.get(WAREHOUSE_STORE_ID_KEY));
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
    public Page<StoreIrRepository.PendingStoreIR> getReadyForTransfer(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return storeIrRepository.findAllReadyForTransfer(getEmpWarehouseId(),pageable);
    }

    @Override
    public Page<StoreIrRepository.PendingStoreIR> getPendingStoreIrVerification(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return storeIrRepository.findAllPendingIrsVerification(getEmpWarehouseId(),
                claimResolver.getUserId(),
                pageable);
    }

    @Override
    public Page<StoreIrRepository.PendingStoreIR> getPendingStoreIrApproval(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return storeIrRepository.findAllPendingIrsApproval(getEmpWarehouseId()
                ,claimResolver.getUserId(),
                pageable);
    }

    @Override
    public Page<StoreIrRepository.PendingStoreIR> getReceiveStoreRequisitions(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return storeIrRepository.findAllReceiveRequisitions(getEmpWarehouseId(),
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
                    List<Map<String,Object>> warehouses = itemDetail
                            .getWarehouses()
                            .get(receiveStockDto.getWarehouseId().toString());
                    if(!warehouses.isEmpty()){
                        Map<String,Object> warehouseStoreInfo = warehouses.get(0);
                        itemService.stockIn(new Item(itemDetail.getId()), irdw.getInTransitReturn(),
                                (Long)warehouseStoreInfo.get(WAREHOUSE_ID_KEY),
                                (Long)warehouseStoreInfo.get(WAREHOUSE_STORE_ID_KEY));
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
        log.info("Rejected IrStore");
    }
}
