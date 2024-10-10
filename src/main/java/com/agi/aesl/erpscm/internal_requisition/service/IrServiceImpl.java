package com.agi.aesl.erpscm.internal_requisition.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class IrServiceImpl implements IrService {

    private static final Integer PAGE_SIZE = 20;
    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private InternalRequisitionRepository irRepository;

    @Autowired
    private UserApplicationValidatorService<InternalRequisition> verificationService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private IrVAHistoryRepository irVAHistoryRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private StoreIrRepository storeIrRepository;

    @Override
    public void createInternalRequisition(Jwt token, String uri, CreateIRDto createDto) {
        claimResolver.setToken(token);
        List<String> cateIds = new ArrayList<>();
        InternalRequisition ir = new InternalRequisition();
        ir.setInternalRequisitionNo(createDto.getIrNo());
        ir.setPriority(createDto.getPriority());
        ir.setCategory(new ItemCategory(createDto.getCategoryId()));
        cateIds.add(createDto.getCategoryId().toString());
        ir.setRequestedBy(new Employee(claimResolver.getEmployee().get().getId()));
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
            }).collect(Collectors.toList()));
            return ird;
        }).collect(Collectors.toList()));

//        String categories = "";
//        categories = String.join(",", cateIds);

        irRepository.save(ir);

        AppliedVADto appliedVADto = verificationService.applyVerifyApprovalProcess(ir, DomainType.IR, IrStatus.COMPLETED.toString(),
                uri, "CATEGORY", cateIds, null);

        if(appliedVADto.getPanels().isEmpty() && appliedVADto.getVerifiers().isEmpty()){
            ir.setIrStatus(IrStatus.COMPLETED);
        }

//        var verifierOp =  verificationService.getVerifiers(token, uri, categories);
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
//                    ir.setStatus(IrStatus.PENDING_VERIFICATION);
//                } else {
//                    ir.setStatus(IrStatus.PENDING);
//                }
//
//            } else {
//                ir.setStatus(IrStatus.PENDING);
//            }
//
//            verificationService.setVerifiers(ir, verifiers, DomainType.IR);
//
//            List<ApprovalSettingQuery.ApprovalPanel> approvalPanels = approvalSettingService
//                    .getModuleWiseApprovalSetting(uri,
//                            Optional.ofNullable(categories),Optional.empty());
//
//            verificationService.setApprovers(ir, approvalPanels, DomainType.IR);

//        }
    }

    @Override
    public Page<?> getAllInternalRequisitions(Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return irRepository.findAllIr(pageable);
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
    public Page<?> getAllClosedIr(Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return irRepository.findAllClosedIr(pageable);
    }

    @Override
    public Page<?> getAllPendingVerificationIrs(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return irRepository.findAllPendingVerificationIr(claimResolver.getEmployee().get().getId(),pageable);
    }

    @Override
    public Page<?> getAllPendingApprovalIrs(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE));
        return irRepository.findAllPendingApprovalIr(claimResolver.getEmployee().get().getId(),pageable);
    }

    @Override
    public Page<?> getAllVerifiedOrApprovedIrs(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE));
        return irRepository.findAllVerifiedOrApprovedIr(pageable);
    }

    @Override
    public Page<?> getAllProcessedIrs(Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return irRepository.findAllProcessedIr(pageable);
    }

    @Override
    public <T> Optional<?> getDetail(Long id, Class<T> t) {
        var irOp = irRepository.findById(id,t);
        Map<String,Object> detailMap = new HashMap<>();
        if(irOp instanceof Optional){
            InternalRequisitionRepository.IrDetail irDetail =    (InternalRequisitionRepository.IrDetail) irOp.get();
            detailMap.put("priority",irDetail.getPriority());
            detailMap.put("id",irDetail.getId());
            detailMap.put("internalRequisitionNo",irDetail.getInternalRequisitionNo());
            detailMap.put("category",irDetail.getCategory());
            detailMap.put("details",irDetail.getDetails());
            detailMap.put("requestedBy",irDetail.getRequestedBy());
            detailMap.put("warehouse",irDetail.getWarehouse());

            List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                    .getVerificationsByDomainTypeAndDomainId(DomainType.IR, irDetail.getId());
            vrs.stream().forEach(verifier->{
                if(verifier.getIsApproval()==false){
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
        return Optional.ofNullable(detailMap);
    }

    @Override
    @Transactional
    public void reviewIr(Jwt token, Long id, ReviewDto reviewDto) {
        claimResolver.setToken(token);
        Optional<InternalRequisition> irOp = irRepository.findById(id);
        if(irOp.isEmpty()){
            throw new RuntimeException("Demand not found");
        }
        InternalRequisition ir = irOp.get();
        ir.setReviewerId(null);

        if(ir.getNextVerifierId()!=null &&  ir.getNextApproverId()==null){
            ir.setIrStatus(IrStatus.PENDING_VERIFICATION);
        }else if(ir.getNextVerifierId()!=null &&  ir.getNextApproverId()!=null){
            ir.setIrStatus(IrStatus.PENDING_APPROVAL);
        }
        commentService.addComment(commentService.prepareComment(
                claimResolver.getEmployee().get(),
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
            throw new RuntimeException("Sorry! IR not found");
        }

        InternalRequisition ir = irOp.get();
        ir.setIrStatus(IrStatus.REJECTED);
        commentService.addComment(commentService.prepareComment(
                claimResolver.getEmployee().get(),
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
            throw new RuntimeException("Sorry! Ir Not found");
        }
        InternalRequisition ir = irOp.get();
        ir.setIsProcessed(true);
        updateIrDto.getDetails().stream().forEach(uid->{
            Optional<InternalRequisitionDetail> irdOp = ir.getDetails().stream().filter(ird->ird.getId().equals(uid.getId())).findFirst();
            if(irdOp.isPresent()){
                InternalRequisitionDetail ird = irdOp.get();
                List<InternalRequisitionDetailWarehouse> irdwList = uid.getWarehouses().stream().map(uidw->{
                    InternalRequisitionDetailWarehouse irdw = new InternalRequisitionDetailWarehouse();
                    irdw.setFromWarehouse(new Warehouse(uidw.getFromWarehouseId()));
                    irdw.setToWarehouse(new Warehouse(uidw.getToWarehouseId()));
                    irdw.setQty(uidw.getQty());
                    irdw.setInternalRequisitionDetail(ird);
                    irdw.setCurrentStock(uidw.getCurrentStock());
                    irdw.setSafetyStock(uidw.getSafetyStock());
                    return irdw;
                }).collect(Collectors.toList());
                ird.setWarehouses(irdwList);
            }
        });

        StoreIR storeIR =  new StoreIR();
        storeIR.setIr(ir);
        storeIR.setIrStatus(IrStatus.PENDING);
        storeIR.setRequestedBy(new Employee(claimResolver.getEmployee().get().getId()));
        storeIrRepository.save(storeIR);
    }

    @Override
    public List<?> getWarehouses(Long id) {

        List<Map<String,Object>> wMaps = new ArrayList<>();
        var itemDetailOp = itemService.getItemDetailWithWarehouse(id);
        if(itemDetailOp instanceof Optional){
            ItemDetail itemDetail = (ItemDetail) itemDetailOp.get();


            itemDetail.getWarehouses().values().stream().forEach(w->{
                Map<String,Object> map = new HashMap<>();
                map.put("safetyStock", itemDetail.getStockThresholdQty());
                AtomicInteger stock = new AtomicInteger() ;
                w.stream().forEach(wi->{
                    map.put("warehouseId",wi.get("warehouseId"));
                    map.put("warehouseName",wi.get("warehouseName"));
                    stock.addAndGet((int)wi.get("stockQty"));
                });
                map.put("currentStock",  stock.get());
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
            throw new RuntimeException("Sorry! IR not found");
        }

        InternalRequisition ir = irOp.get();
        ir.setIrStatus(IrStatus.REJECTED);
    }
}
