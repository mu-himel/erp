package com.agi.aesl.erpscm.internal_requisition.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.internal_requisition.dto.request.CreateIRDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.UpdateIRDetailDto;
import com.agi.aesl.erpscm.internal_requisition.entity.*;
import com.agi.aesl.erpscm.internal_requisition.enums.IrStatus;
import com.agi.aesl.erpscm.internal_requisition.repository.InternalRequisitionRepository;
import com.agi.aesl.erpscm.internal_requisition.repository.IrVAHistoryRepository;
import com.agi.aesl.erpscm.internal_requisition.repository.StoreIrRepository;
import com.agi.aesl.erpscm.inventory.dto.response.ItemDetail;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IrServiceImpl implements IrService {

    private static final Integer PAGE_SIZE = 20;

    private final ClaimResolver claimResolver;


    private final InternalRequisitionRepository irRepository;

    private final UserApplicationValidatorService<InternalRequisition> verificationService;


    private final CommentService commentService;


    private final IrVAHistoryRepository irVAHistoryRepository;


    private final ItemService itemService;


    private final StoreIrRepository storeIrRepository;

    private static final String DATE_START="T00:00:00";
    private static final String DATE_END="T23:59:59";

    private String getEmpId(ClaimResolver claimResolver){
        Employee em = claimResolver.getEmployee().orElse(null);
        return (em!=null)? em.getId() :null;
    }

    private Employee getEmp(ClaimResolver claimResolver){
        return claimResolver.getEmployee().orElse(null);
    }

    @Override
    public void createInternalRequisition(Jwt token, String uri, CreateIRDto createDto) {
        claimResolver.setToken(token);
        List<String> cateIds = new ArrayList<>();
        InternalRequisition ir = new InternalRequisition();
        ir.setDeliveryDate(createDto.getDeliveryDate());
        ir.setInternalRequisitionNo(createDto.getIrNo());
        ir.setPriority(createDto.getPriority());
        ir.setCategory(new ItemCategory(createDto.getCategoryId()));
        cateIds.add(createDto.getCategoryId().toString());
        claimResolver.getEmployee().ifPresent(ir::setRequestedBy);
        ir.setWarehouse(new Warehouse(createDto.getWarehouseId()));
        ir.setIrStatus(IrStatus.PENDING);
        ir.setDetails(createDto.getDetails().stream().map(irDto->{
            InternalRequisitionDetail ird = new InternalRequisitionDetail();
            ird.setDescription(irDto.getDescription());
            ird.setIr(ir);
            ird.setItem(new Item(irDto.getItemId()));
            ird.setBrand(new CategoryBrand(irDto.getBrandId()));
            ird.setItemAttribute(irDto.getItemAttribute());
            ird.setQty(irDto.getQty());
            ird.setSubCategory(new ItemCategory(irDto.getSubCategoryId()));
            cateIds.add(irDto.getSubCategoryId().toString());
            ird.setWarehouses(irDto.getWarehouses().stream().map(irdW->{
                InternalRequisitionDetailWarehouse irdw = new InternalRequisitionDetailWarehouse();
                irdw.setInternalRequisitionDetail(ird);
                irdw.setFromWarehouse(new Warehouse(irdW.getFromWarehouseId()));
                irdw.setToWarehouse(new Warehouse(irdW.getToWarehouseId()));
                irdw.setCurrentStock(irdW.getCurrentStock());
                irdw.setSafetyStock(irdW.getSafetyStock());
                irdw.setQty(irdW.getQty());
                return irdw;
            }).toList());
            return ird;
        }).toList());


        irRepository.save(ir);

        AppliedVADto appliedVADto = verificationService.applyVerifyApprovalProcess(ir, DomainType.IR, IrStatus.COMPLETED.toString(),
                uri, "CATEGORY", cateIds, null);

        if(appliedVADto.getPanels().isEmpty() && appliedVADto.getVerifiers().isEmpty()){
            ir.setIrStatus(IrStatus.COMPLETED);
        }


    }

    @Override
    public Page<InternalRequisitionRepository.IrListInfo> getAllInternalRequisitions(Optional<Integer> page, Optional<Integer> size,
                                                                                     Optional<String> fromDateStr, Optional<String> toDateStr) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateStr.isPresent()){
            fromDate = LocalDateTime.parse(fromDateStr.get()+DATE_START);
        }
        if(toDateStr.isPresent()){
            toDate = LocalDateTime.parse(toDateStr.get()+DATE_END);
        }
        return irRepository.findAllIr(fromDate,toDate,pageable);
    }

    @Override
    public String getNextIrNo() {
        Optional<Long> irOptional = irRepository.findMaxOrderById();
        if(irOptional.isPresent()){
            Long irNo = irOptional.get();
            Long newIrNo = irNo + 1;
            return String.format("%05d",newIrNo);
        }
        return String.format("%05d",1);
    }

    @Override
    public Page<InternalRequisitionRepository.IrListInfo> getAllClosedIr(Optional<Integer> page, Optional<Integer> size,
                                                                         Optional<String> fromDateOp, Optional<String> toDateOp) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateOp.isPresent()){
            fromDate = LocalDateTime.parse(fromDateOp.get()+DATE_START);
        }
        if(toDateOp.isPresent()){
            toDate = LocalDateTime.parse(toDateOp.get()+DATE_END);
        }
        return irRepository.findAllClosedIr(fromDate,toDate,pageable);
    }

    @Override
    public Page<InternalRequisitionRepository.IrVerifierListInfo> getAllPendingVerificationIrs(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                                                               Optional<String> fromDateOp, Optional<String> toDateOp) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateOp.isPresent()){
            fromDate = LocalDateTime.parse(fromDateOp.get()+DATE_START);
        }
        if(toDateOp.isPresent()){
            toDate = LocalDateTime.parse(toDateOp.get()+DATE_END);
        }

        return irRepository.findAllPendingVerificationIr(getEmpId(claimResolver),
                fromDate,toDate,
                pageable);
    }

    @Override
    public Page<InternalRequisitionRepository.IrVerifierListInfo> getAllPendingApprovalIrs(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                                                           Optional<String> fromDateOp, Optional<String> toDateOp) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateOp.isPresent()){
            fromDate = LocalDateTime.parse(fromDateOp.get()+DATE_START);
        }
        if(toDateOp.isPresent()){
            toDate = LocalDateTime.parse(toDateOp.get()+DATE_END);
        }

        return irRepository.findAllPendingApprovalIr(getEmpId(claimResolver),
                fromDate,toDate,pageable);
    }

    @Override
    public Page<InternalRequisitionRepository.IrListInfo> getAllVerifiedOrApprovedIrs(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                                                      Optional<String> fromDateOp, Optional<String> toDateOp) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateOp.isPresent()){
            fromDate = LocalDateTime.parse(fromDateOp.get()+DATE_START);
        }
        if(toDateOp.isPresent()){
            toDate = LocalDateTime.parse(toDateOp.get()+DATE_END);
        }
        return irRepository.findAllVerifiedOrApprovedIr(fromDate, toDate, pageable);
    }

    @Override
    public Page<InternalRequisitionRepository.IrListInfo> getAllProcessedIrs(Optional<Integer> page, Optional<Integer> size,
                                                                             Optional<String> fromDateOp, Optional<String> toDateOp) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateOp.isPresent()){
            fromDate = LocalDateTime.parse(fromDateOp.get()+DATE_START);
        }
        if(toDateOp.isPresent()){
            toDate = LocalDateTime.parse(toDateOp.get()+DATE_END);
        }
        return irRepository.findAllProcessedIr(fromDate, toDate,pageable);
    }

    @Override
    public <T> Optional<Map<String,Object>> getDetail(Long id, Class<T> t) {
        var irOp = irRepository.findById(id,t);
        Map<String,Object> detailMap = new HashMap<>();
        if(irOp instanceof Optional && irOp.isPresent()){
            InternalRequisitionRepository.IrDetail irDetail =    (InternalRequisitionRepository.IrDetail) irOp.get();
            detailMap.put("priority",irDetail.getPriority());
            detailMap.put("id",irDetail.getId());
            detailMap.put("deliveryDate",irDetail.getDeliveryDate());
            detailMap.put("internalRequisitionNo",irDetail.getInternalRequisitionNo());
            detailMap.put("category",irDetail.getCategory());
            detailMap.put("details",irDetail.getDetails());
            detailMap.put("requestedBy",irDetail.getRequestedBy());
            detailMap.put("warehouse",irDetail.getWarehouse());



            List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                    .getVerificationsByDomainTypeAndDomainId(DomainType.IR, irDetail.getId());
            vrs.forEach(verifier->{
                if(Boolean.FALSE.equals(verifier.getIsApproval())){
                    verifiers.add(verifier);
                }else{
                    approvers.add(verifier);
                }
            });

            List<?> comments = commentService.getCommentsByDomain(DomainType.IR, irDetail.getId());
            detailMap.put("comments",comments);
            detailMap.put("verifiers", verifiers);
            detailMap.put("approvers",approvers);
        }
        return  Optional.of(detailMap);
    }

    @Override
    @Transactional
    public void reviewIr(Jwt token, Long id, ReviewDto reviewDto) {
        claimResolver.setToken(token);
        Optional<InternalRequisition> irOp = irRepository.findById(id);
        if(irOp.isEmpty()){
            throw new AesException("Demand not found");
        }
        InternalRequisition ir = irOp.get();
        ir.setReviewerId(null);

        if(ir.getNextVerifierId()!=null &&  ir.getNextApproverId()==null){
            ir.setIrStatus(IrStatus.PENDING_VERIFICATION);
        }else if(ir.getNextApproverId()!=null){
            ir.setIrStatus(IrStatus.PENDING_APPROVAL);
        }
        commentService.addComment(commentService.prepareComment(
                getEmp(claimResolver),
                reviewDto.getDomainType(),
                ir.getId(),
                reviewDto.getMessage(),
                reviewDto.getAttachments()
        ));
    }

    @Override
    @Transactional
    public void rejectIr(Jwt token, Long id, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<InternalRequisition> irOp = irRepository.findById(id);
        if(irOp.isEmpty()){
            throw new AesException("Sorry! IR not found");
        }

        InternalRequisition ir = irOp.get();
        ir.setIrStatus(IrStatus.REJECTED);
        commentService.addComment(commentService.prepareComment(
                getEmp(claimResolver),
                DomainType.IR,
                ir.getId(),
                noteDto.getNote(),
                noteDto.getAttachments()
        ));
    }

    @Override
    @Transactional
    public void updateIR(Jwt token, UpdateIRDetailDto updateIrDto) {
        claimResolver.setToken(token);
        Optional<InternalRequisition> irOp = irRepository.findById(updateIrDto.getId());
        if(irOp.isEmpty()){
            throw new AesException("Sorry! Ir Not found");
        }

        InternalRequisition ir = irOp.get();
        ir.setIsProcessed(true);
        updateIrDto.getDetails().forEach(uid->{
            Optional<InternalRequisitionDetail> irdOp = ir.getDetails().stream().filter(ird->ird.getId().equals(uid.getId())).findFirst();
            if(irdOp.isPresent()){
                InternalRequisitionDetail ird = irdOp.get();
                List<InternalRequisitionDetailWarehouse> irdwList = uid.getWarehouses().stream().map(uidw->{
                    if(uidw.getFromWarehouseId()==null){
                        throw new AesException("Sorry! From Warehouse not selected");
                    }
                    if(uidw.getToWarehouseId()==null){
                        throw new AesException("Sorry! To Warehouse not selected");
                    }
                    if(uidw.getQty()==null){
                        throw new AesException("Sorry! Quantity Missing");
                    }
                    InternalRequisitionDetailWarehouse irdw = new InternalRequisitionDetailWarehouse();
                    irdw.setFromWarehouse(new Warehouse(uidw.getFromWarehouseId()));
                    irdw.setToWarehouse(new Warehouse(uidw.getToWarehouseId()));
                    irdw.setQty(uidw.getQty());
                    irdw.setInternalRequisitionDetail(ird);
                    irdw.setCurrentStock(uidw.getCurrentStock());
                    irdw.setSafetyStock(uidw.getSafetyStock());
                    return irdw;
                }).toList();
                ird.setWarehouses(irdwList);
            }
        });

        StoreIR storeIR =  new StoreIR();
        storeIR.setIr(ir);
        storeIR.setIrStatus(IrStatus.PENDING);
        storeIR.setRequestedBy(getEmp(claimResolver));
        storeIrRepository.save(storeIR);
    }

    @Override
    public List<Map<String,Object>> getWarehouses(Long id) {

        List<Map<String,Object>> wMaps = new ArrayList<>();
        var itemDetailOp = itemService.getItemDetailWithWarehouse(id);
        if(itemDetailOp instanceof Optional && itemDetailOp.isPresent()){
            ItemDetail itemDetail =  itemDetailOp.get();


            itemDetail.getWarehouses().values().forEach(w->{
                Map<String,Object> map = new HashMap<>();
                map.put("safetyStock", itemDetail.getStockThresholdQty());
                BigDecimal bi = new BigDecimal(0L);
                for(Map<String,Object> wi : w){
                    map.put("warehouseId",wi.get("warehouseId"));
                    map.put("warehouseName",wi.get("warehouseName"));
                    BigDecimal stock = (BigDecimal) wi.get("stockQty");

                    bi = bi.add(stock);
                }
                map.put("currentStock",  bi);
                wMaps.add(map);
            });


        }
        return wMaps;
    }

    @Transactional
    private void saveHistory(InternalRequisition ir, String id, IrStatus status){
        IrVAHistory irVAHistory = new IrVAHistory();
        irVAHistory.setIr(ir);
        irVAHistory.setEmployee(new Employee(id));
        irVAHistory.setIrStatus(status);
        irVAHistoryRepository.save(irVAHistory);
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<InternalRequisition> irOptional  = irRepository.findById(id);
        if(irOptional.isPresent()){
            InternalRequisition ir = irOptional.get();
            saveHistory(ir, verification.getVerifier().getId(), IrStatus.VERIFIED);
            ir.setNextVerifierId(nextVerifier.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<InternalRequisition> irOptional  = irRepository.findById(id);
        if(irOptional.isPresent()){
            InternalRequisition ir = irOptional.get();
            saveHistory(ir, verification.getVerifier().getId(), IrStatus.APPROVED);
            ir.setNextApproverId(nextApprover.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<InternalRequisition> irOp  = irRepository.findById(id);
        if(irOp.isPresent()){
            InternalRequisition ir = irOp.get();
            if(firstApprover.isPresent()){
                ir.setNextApproverId(firstApprover.get().getVerifier().getId());
                ir.setIrStatus(IrStatus.PENDING_APPROVAL);
            }else {
                ir.setIrStatus(IrStatus.VERIFIED);

            }
            saveHistory(ir, ir.getNextVerifierId(),IrStatus.VERIFIED);

        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<InternalRequisition> irOptional  = irRepository.findById(id);
        if(irOptional.isPresent()){
            InternalRequisition ir = irOptional.get();
            ir.setIrStatus(IrStatus.APPROVED);
            saveHistory(ir, ir.getNextApproverId(), IrStatus.APPROVED);
        }
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<InternalRequisition> irOptional  = irRepository.findById(domainId);
        if(irOptional.isPresent()){
            InternalRequisition ir = irOptional.get();
            ir.setReviewerId(reviewer.getId());
            ir.setIrStatus(IrStatus.REVIEW);
            ir.setReviewDate(LocalDateTime.now());
        }
    }

    @Override
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {
        Optional<InternalRequisition> irOp = irRepository.findById(domainId);
        if(irOp.isEmpty()){
            throw new AesException("Sorry! IR not found");
        }

        InternalRequisition ir = irOp.get();
        ir.setIrStatus(IrStatus.REJECTED);
    }
}
