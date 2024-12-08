package com.agi.aesl.erpscm.store_receive.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationWriterService;
import com.agi.aesl.erpscm.goods_receive.dto.request.GoodReceiveNoteDto;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.repository.GrnDetailRepository;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import com.agi.aesl.erpscm.inventory.dto.response.ItemDetail;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import com.agi.aesl.erpscm.modules.service.ModuleService;
import com.agi.aesl.erpscm.store_receive.dto.SrnDto;
import com.agi.aesl.erpscm.store_receive.entity.SrnVerifyApprovalHistory;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveDetail;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveNote;
import com.agi.aesl.erpscm.store_receive.enums.SrnStatus;
import com.agi.aesl.erpscm.store_receive.repository.SrnRepository;
import com.agi.aesl.erpscm.store_receive.repository.SrnVerifyApprovalHistoryRepository;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
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
public class SrnServiceImpl implements SrnService{

    @Autowired
    private SrnRepository srnRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private GrnService grnService;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private SrnVerifyApprovalHistoryRepository srnVerifyApprovalHistoryRepository;

    @Autowired
    private UserApplicationValidatorService<StoreReceiveNote> verificationService;

    @Autowired
    private IntegrationWriterService integrationWriterService;

    @Autowired
    private GrnDetailRepository grnDetailRepository;

    @Override
    @Transactional
    public void addSrn(Jwt token, SrnDto srnDto) {
        claimResolver.setToken(token);
        StoreReceiveNote storeReceiveNote = new StoreReceiveNote();
        List<String> ids = new ArrayList<>();
        storeReceiveNote.setSrnNo(srnDto.getSrnNo());
        storeReceiveNote.setComment(srnDto.getComment());
        storeReceiveNote.setEmployee(new Employee(claimResolver.getEmployee().get().getId()));
        Optional<GoodReceiveNote> goodReceiveNoteOp = grnService.getByGrnNo(srnDto.getSrnNo());
        if(goodReceiveNoteOp.isEmpty()){
            throw new RuntimeException("Grn not found");
        }
        GoodReceiveNote grn = goodReceiveNoteOp.get();
        storeReceiveNote.setCostCenter(srnDto.getCostCenter());
        storeReceiveNote.setGrn(grn);
        storeReceiveNote.setSrnDetails(srnDto.getSrnDetails().stream().map(storeReceiveDetailDto -> {
            StoreReceiveDetail storeReceiveDetail = new StoreReceiveDetail();
            Optional<Item> itemOp = itemService.getItemDetail(storeReceiveDetailDto.getItem().getId());
            if(itemOp.isEmpty()) {
                throw new RuntimeException("Sorry! Item not found");
            }
            Optional<GoodReceiveItemDetail> goodReceiveItemDetailOptional = grnDetailRepository.findById(storeReceiveDetailDto.getGoodReceiveItemDetail().getId());
            if(goodReceiveItemDetailOptional.isEmpty()){
                throw new RuntimeException("Good Receive Detail not found");
            }
            Item item = itemOp.get();
            storeReceiveDetail.setItem(item);
            storeReceiveDetail.setWarehouse(storeReceiveDetailDto.getWarehouse());
            storeReceiveDetail.setWarehouseStore(storeReceiveDetailDto.getWarehouseStore());

            storeReceiveDetail.setGoodReceiveItemDetail(goodReceiveItemDetailOptional.get());
            storeReceiveDetail.setStoreReceiveNote(storeReceiveNote);
            storeReceiveDetail.setCostCenter(storeReceiveDetailDto.getCostCenter());
            BigDecimal stockInQty = storeReceiveDetailDto.getStockInQty()!=null? storeReceiveDetailDto.getStockInQty(): new BigDecimal(0);
            storeReceiveDetail.setStockInQty(stockInQty);
            ids.add(item.getItemCategory().getId().toString());
            ids.add(item.getItemParentCategory().getId().toString());

//              stock in process
//            Optional<ItemDetail> itemDetailOp = (Optional<ItemDetail>)itemService.getItemDetailWithWarehouse(item.getId());
//
//            if(itemDetailOp.isPresent()) {
            //        grn.setIsReceivedByStore(true);
//               grn.setGrnStatus(GrnStatus.COMPLETED);
//                ItemDetail itemDetail = itemDetailOp.get();
//                List<Map<String,Object>> stores = itemDetail
//                        .getWarehouses().get(claimResolver.getEmployee().get().getWarehouseId().toString());
//                Optional<Map<String,Object>> store = stores.stream().filter(
//                        stringObjectMap -> !((String)stringObjectMap.get("warehouseStoreName"))
//                                .toLowerCase().contains("finish goods")
//                ).findFirst();
//                store.ifPresent(stringObjectMap -> {
//                    Long warehouseStoreId = (Long)stringObjectMap.get("warehouseStoreId");
//                    itemService.stockIn(item, storeReceiveDetailDto.getStockInQty(),
//                            claimResolver.getEmployee().get().getWarehouseId(),
//                            warehouseStoreId);
//                });
//            }
            return storeReceiveDetail;
        }).collect(Collectors.toList()));

        srnRepository.save(storeReceiveNote);

        String uri="inventory-management/good-receive/store-receive-note";
        if(ids.size()>0 && !uri.isBlank()) {

            AppliedVADto result = verificationService.applyVerifyApprovalProcess(storeReceiveNote, DomainType.SRN,
                    SrnStatus.APPROVED.toString(), uri,
                    "CATEGORY", ids, null);

            if(result.getVerifiers().isEmpty() && result.getPanels().isEmpty()){
                storeReceiveNote.setSrnStatus(SrnStatus.COMPLETED);
                storeReceiveNote.getGrn().setGrnStatus(GrnStatus.COMPLETED);
                storeReceiveNote.setSrnDetails(storeReceiveNote.getSrnDetails().stream().map(srnd->{
                    srnd.setStockInQty(srnd.getStockInQty());
                    storeInItem(grn, srnd);
                    return srnd;
                }).collect(Collectors.toList()));
//                integrationWriterService.purchaseReceived(claimResolver.getToken(),storeReceiveNote);
            }

//            Optional<VerifierConfig> verifierOp = verificationService.getVerifiers(claimResolver, uri,
//                    "CATEGORY", String.join(",", ids));
//
//            List<VerifierInfo> verifiers = getVerifiers(storeReceiveNote, verifierOp);
//            List<ApprovalPanel> panels = getApprovalPanels(claimResolver, uri, String.join(",", ids));
//            verificationService.setVerifiers(storeReceiveNote, verifiers, DomainType.SRN,
//                    null);
//            if (verifiers.size() == 0 && panels.size() > 0) {
//                storeReceiveNote.setSrnStatus(SrnStatus.PENDING_APPROVAL);
//                Optional<ApprovalPanel> firstPanel = panels.stream().findFirst();
//                if (firstPanel.isPresent()) {
//                    ApprovalPanel panel = firstPanel.get();
////                    demandMailService.prepareMailContent(panel.getName(), "Approval", demand);
////                    demandMailService.sentMail(panel.getEmail(),"Pending Demand Approval Request");
//                    storeReceiveNote.setNextApproverId(panel.getUserId());
//                }
//            }
//            verificationService.setApprovers(storeReceiveNote, verifiers,panels, DomainType.SRN,null);
        }

    }

    @Transactional
    private List<VerifierInfo> getVerifiers(StoreReceiveNote storeReceiveNote, Optional<VerifierConfig> verifierOp) {
        List<VerifierInfo> verifiers = new ArrayList<>();
        if(verifierOp.isPresent()){
            VerifierConfig verification = verifierOp.get();
            verifiers = verification.getVerifiers();
            Boolean verificationRequired = verification.getVerificationRequired();
            if(verificationRequired!=null && verificationRequired==true && verifiers!=null && verifiers.size()>0){
                storeReceiveNote.setSrnStatus(SrnStatus.PENDING_VERIFICATION);
            }else{
                storeReceiveNote.setSrnStatus(SrnStatus.VERIFIED);
            }

        }else{
            storeReceiveNote.setSrnStatus(SrnStatus.VERIFIED);
        }
        return verifiers;
    }

    @Transactional
    private List<ApprovalPanel> getApprovalPanels(ClaimResolver claimResolver,String uri, String categories) {
        List<ApprovalPanel> approvalPanels = moduleService.getModuleWiseApprovalSetting(claimResolver,uri,
                Optional.ofNullable(categories),Optional.empty());
        return approvalPanels;
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
        List<String> status = new ArrayList<>();
        status.add(GrnStatus.QC_PASS.toString());
        status.add(GrnStatus.READY_FOR_STORE.toString());
        status.add(SrnStatus.APPROVED.toString());
        status.add(SrnStatus.VERIFIED.toString());
        return srnRepository.findAllSrnByStatus(status, fromDateObj, toDateObj,
                pageable);
    }

    @Override
    public Page<?> getPendingVerifications(Jwt token, Optional<String> fromDate, Optional<String> toDate,
                                           Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        String uri="";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + "T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get() + "T23:59:59");
        }
        return srnRepository.findAllPendingVerification(claimResolver.getUserId(),fromDateObj,toDateObj,pageable);
    }

    @Override
    public Page<?> getPendingApprovals(Jwt token, Optional<String> fromDate, Optional<String> toDate,
                                       Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        String uri="";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + "T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get() + "T23:59:59");
        }
        return srnRepository.findAllPendingApproval(claimResolver.getUserId(),fromDateObj,toDateObj,pageable);

    }

    @Override
    public Page<?> getAllComplete(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<String> fromDate, Optional<String> toDate) {
        claimResolver.setToken(token);
        String uri="";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + "T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get() + "T23:59:59");
        }
        List<String> status = new ArrayList<>();
        status.add(GrnStatus.COMPLETED.toString());
        status.add(SrnStatus.VERIFIED.toString());
        status.add(SrnStatus.APPROVED.toString());
        status.add(SrnStatus.REJECTED.toString());
        return srnRepository.findCompletedSrnByStatus(status,
                fromDateObj, toDateObj,pageable);
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<StoreReceiveNote> srnOp = srnRepository.findById(id);
        if(srnOp.isPresent()){
            StoreReceiveNote srn = srnOp.get();
//            demandMailService.prepareMailContent(verificationResponse.getVerifier().getEmployeeName(),"Approval",demand);
//            demandMailService.sentMail(verificationResponse.getVerifier().getEmailAddress(),"Pending Demand Approval Request");
            SrnVerifyApprovalHistory svah = new SrnVerifyApprovalHistory();
            svah.setEmployee(verification.getVerifier());
            svah.setSrnStatus(SrnStatus.VERIFIED);
            svah.setStoreReceiveNote(srn);
            srnVerifyApprovalHistoryRepository.save(svah);

            srn.setNextVerifierId(nextVerifier.getVerifier().getId());

        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<StoreReceiveNote> srnOp = srnRepository.findById(id);
        if(srnOp.isPresent()) {
            StoreReceiveNote srn = srnOp.get();
//            demandMailService.prepareMailContent(verificationResponse.getVerifier().getEmployeeName(),"Approval",demand);
//            demandMailService.sentMail(verificationResponse.getVerifier().getEmailAddress(),"Pending Demand Approval Request");
            SrnVerifyApprovalHistory svah = new SrnVerifyApprovalHistory();
            svah.setEmployee(verification.getVerifier());
            svah.setSrnStatus(SrnStatus.APPROVED);
            svah.setStoreReceiveNote(srn);
            srnVerifyApprovalHistoryRepository.save(svah);
            srn.setNextApproverId(nextApprover.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<StoreReceiveNote> srnOp = srnRepository.findById(id);
        if(srnOp.isPresent()) {
            StoreReceiveNote srn = srnOp.get();

            SrnVerifyApprovalHistory svah = new SrnVerifyApprovalHistory();
            svah.setEmployee(new Employee(srn.getNextVerifierId()));
            svah.setSrnStatus(SrnStatus.VERIFIED);
            svah.setStoreReceiveNote(srn);
            srnVerifyApprovalHistoryRepository.save(svah);

            if (firstApprover.isPresent()) {
                srn.setNextApproverId(firstApprover.get().getVerifier().getId());
                srn.setSrnStatus(SrnStatus.PENDING_APPROVAL);
            } else {
                srn.setSrnStatus(SrnStatus.VERIFIED);
                GoodReceiveNote grn = srn.getGrn();
                grn.setGrnStatus(GrnStatus.COMPLETED);
                srn.setSrnDetails(srn.getSrnDetails().stream().map(srnd->{
                    srnd.setStockInQty(srnd.getStockInQty());
                    storeInItem(grn, srnd);
                    return srnd;
                }).collect(Collectors.toList()));
                integrationWriterService.purchaseReceived(claimResolver.getToken(),srn);
            }
        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<StoreReceiveNote> srnOp = srnRepository.findById(id);
        if(srnOp.isPresent()) {
            StoreReceiveNote srn = srnOp.get();
            srn.setSrnStatus(SrnStatus.APPROVED);
            GoodReceiveNote grn = srn.getGrn();
            grn.setGrnStatus(GrnStatus.COMPLETED);
            srn.setSrnDetails(srn.getSrnDetails().stream().map(srnd->{
                srnd.setStockInQty(srnd.getStockInQty());
                storeInItem(grn, srnd);
                return srnd;
            }).collect(Collectors.toList()));



            SrnVerifyApprovalHistory svah = new SrnVerifyApprovalHistory();
            svah.setEmployee(new Employee(srn.getNextApproverId()));
            svah.setSrnStatus(SrnStatus.APPROVED);
            svah.setStoreReceiveNote(srn);
            srnVerifyApprovalHistoryRepository.save(svah);
            integrationWriterService.purchaseReceived(claimResolver.getToken(),srn);
        }
    }

    @Transactional
    private void storeInItem(GoodReceiveNote grn, StoreReceiveDetail srnd) {
        Optional<Item> itemOp = itemService.getItemDetail(srnd.getItem().getId());
        if(itemOp.isEmpty()) {
            throw new RuntimeException("Sorry! Item not found");
        }
        Item item = itemOp.get();
        Optional<ItemDetail> itemDetailOp = (Optional<ItemDetail>)itemService.getItemDetailWithWarehouseWithoutInTransit(item.getId());
        if(itemDetailOp.isPresent()) {
            grn.setIsReceivedByStore(true);
            grn.setGrnStatus(GrnStatus.COMPLETED);
            ItemDetail itemDetail = itemDetailOp.get();
            List<Map<String,Object>> stores = itemDetail
                    .getWarehouses().get(claimResolver.getEmployee().get().getWarehouseId().toString());
            Optional<Map<String,Object>> store = stores.stream().filter(
                    stringObjectMap -> !((String)stringObjectMap.get("warehouseStoreName"))
                            .toLowerCase().contains("finish goods")
            ).findFirst();
            store.ifPresent(stringObjectMap -> {
                Long warehouseStoreId = (Long)stringObjectMap.get("warehouseStoreId");
                itemService.stockIn(item, srnd.getStockInQty(),
                        claimResolver.getEmployee().get().getWarehouseId(),
                        warehouseStoreId);
            });
        }
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<StoreReceiveNote> srnOp = srnRepository.findById(domainId);
        if(srnOp.isPresent()) {
            StoreReceiveNote srn = srnOp.get();
            if(!srn.getSrnStatus().equals(SrnStatus.REVIEW)) {
                srn.setReviewPrevStatus(srn.getSrnStatus());
                srn.setSrnStatus(SrnStatus.REVIEW);
            }
            srn.setReviewDate(LocalDateTime.now());
            srn.setReviewerId(reviewer.getId());

        }
    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {
        Optional<UserApplicationValidation> verificationOp = verificationService
                .getVerificationsByDomainTypeAndDomainIdAndVerifierId(DomainType.SRN,domainId,verifier);

        if(verificationOp.isPresent()){
            UserApplicationValidation validation = verificationOp.get();
            validation.setVerified(true);
        }

        Optional<StoreReceiveNote> srnOp = srnRepository.findById(domainId);
        if(srnOp.isPresent()){
            StoreReceiveNote qc = srnOp.get();
            qc.setSrnStatus(SrnStatus.REJECTED);
        }
    }

    @Override
    @Transactional
    public void review(Jwt token, Long id, ReviewDto reviewDto) {
        claimResolver.setToken(token);
        Optional<StoreReceiveNote> srnOp = srnRepository.findById(id);
        if(srnOp.isEmpty()){
            throw new RuntimeException("QC not found");
        }
        StoreReceiveNote srn = srnOp.get();
        srn.setReviewerId(null);
        if(srn.getReviewPrevStatus()!=null) {
            srn.setSrnStatus(srn.getReviewPrevStatus());
        }
        srn.setReviewPrevStatus(null);
        srn.setReviewDate(LocalDateTime.now());

        commentService.addComment(commentService.prepareComment(
                claimResolver.getEmployee().get(),
                reviewDto.getDomainType(),
                reviewDto.getActionType(),
                srn.getId(),
                reviewDto.getMessage(),
                reviewDto.getAttachments()
        ));
    }

    @Override
    public Optional<?> getDetail(Long id) {
        Optional<SrnRepository.SrnDetail> srnOp = srnRepository.findById(id, SrnRepository.SrnDetail.class);
        if(srnOp.isPresent()){
            SrnRepository.SrnDetail srn = srnOp.get();

        List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
        List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
        List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                .getVerificationsByDomainTypeAndDomainId(DomainType.SRN, srn.getId());
        vrs.stream().forEach(verifier->{
            if(verifier.getIsApproval()==false){
                verifiers.add(verifier);
            }else{
                approvers.add(verifier);
            }
        });

        List<?> comments = commentService.getCommentsByDomain(DomainType.SRN, srn.getId());

        Map<String,Object> detail = new HashMap<>();
        detail.put("approvers",approvers);
        detail.put("comments",comments);
        detail.put("verifiers",verifiers);
        detail.put("detail",srn);
        return Optional.ofNullable(detail);
        }
        return Optional.empty();
    }
}
