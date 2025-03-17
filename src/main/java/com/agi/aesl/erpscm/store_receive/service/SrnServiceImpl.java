package com.agi.aesl.erpscm.store_receive.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationWriterService;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnMode;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.repository.GrnDetailRepository;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.modules.service.ModuleService;
import com.agi.aesl.erpscm.store_receive.dto.SrnDto;
import com.agi.aesl.erpscm.store_receive.entity.SrnVerifyApprovalHistory;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveDetail;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveNote;
import com.agi.aesl.erpscm.store_receive.enums.SrnStatus;
import com.agi.aesl.erpscm.store_receive.repository.SrnQuery;
import com.agi.aesl.erpscm.store_receive.repository.SrnRepository;
import com.agi.aesl.erpscm.store_receive.repository.SrnVerifyApprovalHistoryRepository;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class SrnServiceImpl implements SrnService{


    private final SrnRepository srnRepository;

    private final ItemService itemService;

    private final GrnService grnService;

    private final ClaimResolver claimResolver;

    private final CommentService commentService;

    private final ModuleService moduleService;

    private final SrnVerifyApprovalHistoryRepository srnVerifyApprovalHistoryRepository;

    private final UserApplicationValidatorService<StoreReceiveNote> verificationService;

    private final IntegrationWriterService integrationWriterService;

    private final GrnDetailRepository grnDetailRepository;

    @Resource
    private SrnStoreService srnStoreService;

    private static final String DATE_TIME_START="T00:00:00";
    private static final String DATE_TIME_END="T23:59:59";

    private Long getEmpWarehouseId(){
        Employee emp = claimResolver.getEmployee().orElse(null);
        return emp!=null? emp.getWarehouseId() : null;
    }

    @Override
    @Transactional
    public void addSrn(Jwt token, SrnDto srnDto) {
        claimResolver.setToken(token);

        StoreReceiveNote storeReceiveNote = new StoreReceiveNote();
        List<String> ids = new ArrayList<>();
        storeReceiveNote.setSrnNo(srnDto.getSrnNo());
        storeReceiveNote.setComment(srnDto.getComment());
        claimResolver.getEmployee().ifPresent(storeReceiveNote::setEmployee);

        Optional<GoodReceiveNote> goodReceiveNoteOp = grnService.getByGrnNo(srnDto.getSrnNo());
        if(goodReceiveNoteOp.isEmpty()){
            throw new AesException("Grn not found");
        }
        GoodReceiveNote grn = goodReceiveNoteOp.get();
        storeReceiveNote.setCostCenter(srnDto.getCostCenter());
        storeReceiveNote.setGrn(grn);
        srnStoreService.setDetail(storeReceiveNote,itemService,srnDto,ids,grnDetailRepository);

        srnRepository.save(storeReceiveNote);

        String uri="inventory-management/good-receive/store-receive-note";
        if(ids.isEmpty()) {

            AppliedVADto result = verificationService.applyVerifyApprovalProcess(storeReceiveNote, DomainType.SRN,
                    SrnStatus.APPROVED.toString(), uri,
                    "CATEGORY", ids, null);

            if(result.getVerifiers().isEmpty() && result.getPanels().isEmpty()){
                storeReceiveNote.setSrnStatus(SrnStatus.COMPLETED);
                storeReceiveNote.getGrn().setGrnStatus(GrnStatus.COMPLETED);
                storeReceiveNote.setSrnDetails(storeReceiveNote.getSrnDetails().stream().map(srnd->{
                    srnd.setStockInQty(srnd.getStockInQty());
                    srnStoreService.storeInItem(grn, srnd,itemService,getEmpWarehouseId());
                    return srnd;
                }).toList());

                if(storeReceiveNote.getGrn().getGrnMode().equals(GrnMode.MANUAL)){
                    integrationWriterService.purchaseReceivedManual(claimResolver.getToken(),storeReceiveNote);
                }

                if(storeReceiveNote.getGrn().getGrnMode().equals(GrnMode.AUTO)){
                    integrationWriterService.purchaseReceived(claimResolver.getToken(),storeReceiveNote);
                }
            }
        }

    }


    @Override
    public List<SrnQuery.PendingDemandList> getPendingDemandListBySrnItems(Long id) {
        return srnRepository.getPendingDemandsBySrnForSrnItems(id);
    }

    @Override
    public List<SrnQuery.PendingDemandList> getPendingDemandListBySrnItems(Jwt token, String attributes) {
        claimResolver.setToken(token);
        AtomicReference<Long> warehouseId= new AtomicReference<>();

        claimResolver.getEmployee().ifPresent( emp->

                warehouseId.set(emp.getWarehouseId())
        );
        if(warehouseId.get() ==null){
            throw new AesException("Sorry! warehouse information missing for user");
        }
        return srnRepository.getPendingDemandsBySrnForSrnItems(warehouseId.get(),attributes);
    }

    @Override
    public Page<SrnRepository.StoreReceiveNoteInfo> getAll(Jwt token, Pageable pageable,
                                                           Optional<String> grnNo, Optional<Long> categoryId, Optional<Long> receivedQty,
                                                           Optional<String> fromDate, Optional<String> toDate) {
        claimResolver.setToken(token);
        if(claimResolver.getEmployee().isEmpty()){
            throw new AesException("Sorry! Store Profile Required");
        }

        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + DATE_TIME_START);
            toDateObj = LocalDateTime.parse(toDate.get() + DATE_TIME_END);
        }
        List<String> status = new ArrayList<>();
        status.add(SrnStatus.PENDING_VERIFICATION.toString());
        status.add(SrnStatus.PENDING_APPROVAL.toString());
        status.add(SrnStatus.REVIEW.toString());

        return srnRepository.findAllSrnByStatus(getEmpWarehouseId(),status, grnNo.orElse(null),
                categoryId.orElse(null),receivedQty.orElse(null),
                fromDateObj, toDateObj,
                pageable);
    }

    @Override
    public Page<SrnRepository.StoreReceiveNoteInfo> getPendingVerifications(Jwt token, Optional<String> fromDate, Optional<String> toDate,
                                                                            Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + DATE_TIME_START);
            toDateObj = LocalDateTime.parse(toDate.get() + DATE_TIME_END);
        }
        return srnRepository.findAllPendingVerification(claimResolver.getUserId(),fromDateObj,toDateObj,pageable);
    }

    @Override
    public Page<SrnRepository.StoreReceiveNoteInfo> getPendingApprovals(Jwt token, Optional<String> fromDate, Optional<String> toDate,
                                                                        Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);

        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + DATE_TIME_START);
            toDateObj = LocalDateTime.parse(toDate.get() + DATE_TIME_END);
        }
        return srnRepository.findAllPendingApproval(claimResolver.getUserId(),fromDateObj,toDateObj,pageable);

    }

    @Override
    public Page<SrnRepository.StoreReceiveNoteInfo> getAllComplete(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                                   Optional<String> grnNo, Optional<String> fromDate, Optional<String> toDate) {
        claimResolver.setToken(token);
        if(claimResolver.getEmployee().isEmpty()){
            throw new AesException("Sorry! Store Profile Required");
        }
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + DATE_TIME_START);
            toDateObj = LocalDateTime.parse(toDate.get() + DATE_TIME_END);
        }
        List<String> status = new ArrayList<>();
        status.add(GrnStatus.COMPLETED.toString());
        status.add(SrnStatus.VERIFIED.toString());
        status.add(SrnStatus.APPROVED.toString());
        status.add(SrnStatus.REJECTED.toString());
        return srnRepository.findCompletedSrnByStatus(getEmpWarehouseId(),status, grnNo.orElse(null),
                fromDateObj, toDateObj,pageable);
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<StoreReceiveNote> srnOp = srnRepository.findById(id);
        if(srnOp.isPresent()){
            StoreReceiveNote srn = srnOp.get();
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
                    srnStoreService.storeInItem(grn, srnd,itemService,getEmpWarehouseId());
                    return srnd;
                }).toList());

                if(srn.getGrn().getGrnMode().equals(GrnMode.MANUAL)){
                    integrationWriterService.purchaseReceivedManual(claimResolver.getToken(),srn);
                }

                if(srn.getGrn().getGrnMode().equals(GrnMode.AUTO)){
                    integrationWriterService.purchaseReceived(claimResolver.getToken(),srn);
                }
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
                srnStoreService.storeInItem(grn, srnd,itemService,getEmpWarehouseId());
                return srnd;
            }).toList());



            SrnVerifyApprovalHistory svah = new SrnVerifyApprovalHistory();
            svah.setEmployee(new Employee(srn.getNextApproverId()));
            svah.setSrnStatus(SrnStatus.APPROVED);
            svah.setStoreReceiveNote(srn);
            srnVerifyApprovalHistoryRepository.save(svah);
            if(srn.getGrn().getGrnMode().equals(GrnMode.MANUAL)){
                integrationWriterService.purchaseReceivedManual(claimResolver.getToken(),srn);
            }

            if(srn.getGrn().getGrnMode().equals(GrnMode.AUTO)){
                integrationWriterService.purchaseReceived(claimResolver.getToken(),srn);
            }
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
            throw new AesException("QC not found");
        }
        StoreReceiveNote srn = srnOp.get();
        srn.setReviewerId(null);
        if(srn.getReviewPrevStatus()!=null) {
            srn.setSrnStatus(srn.getReviewPrevStatus());
        }
        srn.setReviewPrevStatus(null);
        srn.setReviewDate(LocalDateTime.now());

        claimResolver.getEmployee().ifPresent(employee->
        commentService.addComment(commentService.prepareComment(
                employee,
                reviewDto.getDomainType(),
                reviewDto.getActionType(),
                srn.getId(),
                reviewDto.getMessage(),
                reviewDto.getAttachments()
        )));
    }

    @Override
    public Optional<Map<String,Object>> getDetail(Long id) {
        Optional<SrnRepository.SrnDetail> srnOp = srnRepository.findById(id, SrnRepository.SrnDetail.class);
        if(srnOp.isPresent()){
            SrnRepository.SrnDetail srn = srnOp.get();

        List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
        List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
        List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                .getVerificationsByDomainTypeAndDomainId(DomainType.SRN, srn.getId());
        vrs.forEach(verifier->{
            if(Boolean.FALSE.equals(verifier.getIsApproval())){
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
        return Optional.of(detail);
        }
        return Optional.empty();
    }
}
