package com.agi.aesl.erpscm.quality_control.service;

import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccountVerifyApprovalHistory;
import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.enums.QcType;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import com.agi.aesl.erpscm.modules.service.ModuleService;
import com.agi.aesl.erpscm.quality_control.dto.request.QcDto;
import com.agi.aesl.erpscm.quality_control.entity.QcVerifyApprovalHistory;
import com.agi.aesl.erpscm.quality_control.entity.QualityControl;
import com.agi.aesl.erpscm.quality_control.enums.QcStatus;
import com.agi.aesl.erpscm.quality_control.repository.QcRepository;
import com.agi.aesl.erpscm.quality_control.repository.QcVerifyApprovalHistoryRepository;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class QcServiceImpl implements QcService{

    @Autowired
    private QcRepository qcRepository;

    @Autowired
    @Lazy
    private GrnService grnService;

    @Autowired
    private UserApplicationValidatorService<QualityControl> verificationService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private QcVerifyApprovalHistoryRepository qcVerifyApprovalHistoryRepository;

    @Override
    @Transactional
    public void addQc(Jwt token, QcDto controlDto) throws IllegalAccessException {

        claimResolver.setToken(token);
        AtomicReference<Boolean> error = new AtomicReference<>(false);

        controlDto.getKpis().stream().forEach(qualityControlKpi -> {
            if(qualityControlKpi.getQcType().equals(null)){
                error.set(true);
            }
        });
        if(error.get()){
            throw new RuntimeException("Inspection summary should be submit for all kpi");
        }

        if(controlDto.getGrn()==null || controlDto.getGrn().getId() == null){
            throw new RuntimeException("Good Receive Note Reference missing");
        }

        Optional<GoodReceiveNote> goodReceiveNoteOptional = (Optional<GoodReceiveNote>)grnService.getGRNById(controlDto.getGrn().getId(),true);
        if(goodReceiveNoteOptional.isEmpty()){
            throw new RuntimeException("Good Receive note not found");
        }

        GoodReceiveNote grn = goodReceiveNoteOptional.get();

        controlDto.getQcItemDetails().stream().forEach(qcItemDetail -> {
            Optional<GoodReceiveItemDetail> grnItemDetail = grn.getGoodReceiveItemDetails().stream().filter(
                    goodReceiveItemDetail -> goodReceiveItemDetail.getItem().getId().equals(qcItemDetail.getId())
            ).findFirst();

            if(grnItemDetail.isPresent()){
                GoodReceiveItemDetail goodReceiveItemDetail = grnItemDetail.get();
                goodReceiveItemDetail.setDeclaredQty(qcItemDetail.getDeclaredQty());
                goodReceiveItemDetail.setInspectedQty(qcItemDetail.getInspectedQty());
                grnService.updateGrnItemDetail(goodReceiveItemDetail);
            }

        });


        QualityControl qualityControl = new QualityControl();

        qualityControl.setComment(controlDto.getComment());
        qualityControl.setCreatedBy(new Employee(claimResolver.getEmployee().get().getId()));
        qualityControl.setWarehouse(new Warehouse(claimResolver.getEmployee().get().getWarehouseId()));
        qualityControl.setGoodReceiveNote(grn);

        AtomicReference<Integer> qcPassCount = new AtomicReference<>(0);
        AtomicReference<Integer> qcFailCount = new AtomicReference<>(0);
        if(controlDto.getKpis().size()==3 && controlDto.getQcStatus()== QcStatus.APPROVED) {

            qualityControl.setQcStatus(QcStatus.APPROVED);

            qualityControl.setQualityControlKpis(
                    controlDto.getKpis().stream().map(qualityControlKpi -> {
                        qualityControlKpi.setQualityControl(qualityControl);
                        if (qualityControlKpi.getQcType() == QcType.PASS) {
                            qcPassCount.getAndSet(qcPassCount.get() + 1);
                        }
                        return qualityControlKpi;
                    }).collect(Collectors.toList())
            );

        }else{

            qualityControl.setQcStatus(QcStatus.REJECTED);
        }

        qcRepository.save(qualityControl);

        if(qcPassCount.get().equals(3)){
            grn.setGrnStatus(GrnStatus.READY_FOR_STORE);
        }
        if(qualityControl.getQcStatus().equals(QcStatus.REJECTED) ||  qcFailCount.get()>0){
            grn.setGrnStatus(GrnStatus.QC_FAILED);
        }
        if(qcPassCount.get()==0 && qcFailCount.get()==0 ){
            grn.setGrnStatus(GrnStatus.QC_HOLD);
        }

        List<String> ids = new ArrayList<>();
        String uri="";
        if(ids.size()>0 && !uri.isBlank()) {
            Optional<VerifierConfig> verifierOp = verificationService.getVerifiers(claimResolver, uri,
                    "CATEGORY", String.join(",", ids));

            List<VerifierInfo> verifiers = getVerifiers(qualityControl, verifierOp);
            List<ApprovalPanel> panels = getApprovalPanels(claimResolver, uri, String.join(",", ids));
            verificationService.setVerifiers(qualityControl, verifiers, DomainType.QC,
                    null);
            if (verifiers.size() == 0 && panels.size() > 0) {
                qualityControl.setQcStatus(QcStatus.PENDING_APPROVAL);
                Optional<ApprovalPanel> firstPanel = panels.stream().findFirst();
                if (firstPanel.isPresent()) {
                    ApprovalPanel panel = firstPanel.get();
//                    demandMailService.prepareMailContent(panel.getName(), "Approval", demand);
//                    demandMailService.sentMail(panel.getEmail(),"Pending Demand Approval Request");
                    qualityControl.setNextApproverId(panel.getUserId());
                }
            }
            verificationService.setApprovers(qualityControl, panels, DomainType.QC);
        }
    }

    @Transactional
    private List<VerifierInfo> getVerifiers(QualityControl qualityControl, Optional<VerifierConfig> verifierOp) {
        List<VerifierInfo> verifiers = new ArrayList<>();
        if(verifierOp.isPresent()){
            VerifierConfig verification = verifierOp.get();
            verifiers = verification.getVerifiers();
            Boolean verificationRequired = verification.getVerificationRequired();
            if(verificationRequired!=null && verificationRequired==true && verifiers!=null && verifiers.size()>0){
                qualityControl.setQcStatus(QcStatus.PENDING_VERIFICATION);
            }else{
                qualityControl.setQcStatus(QcStatus.VERIFIED);
            }

        }else{
            qualityControl.setQcStatus(QcStatus.VERIFIED);
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
    public List<?> getQcResultByGrn(Long id) {

        return qcRepository.getQcResultByGrn(id);
    }

    @Override
    @Transactional
    public void rejectQc(Long id) {
        Optional<GoodReceiveNote> goodReceiveNoteOptional = (Optional<GoodReceiveNote>)grnService.getGRNById(id, true);
        if(goodReceiveNoteOptional.isPresent()){
            GoodReceiveNote grn = goodReceiveNoteOptional.get();
            grn.setGrnStatus(GrnStatus.REJECTED);
        }
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<QualityControl> qcOp = qcRepository.findById(id);
        if(qcOp.isPresent()){
            QualityControl qc = qcOp.get();
//            demandMailService.prepareMailContent(verificationResponse.getVerifier().getEmployeeName(),"Approval",demand);
//            demandMailService.sentMail(verificationResponse.getVerifier().getEmailAddress(),"Pending Demand Approval Request");
            QcVerifyApprovalHistory qvah = new QcVerifyApprovalHistory();
            qvah.setEmployee(verification.getVerifier());
            qvah.setQcStatus(QcStatus.VERIFIED);
            qvah.setQualityControl(qc);
            qcVerifyApprovalHistoryRepository.save(qvah);
            qc.setNextVerifierId(nextVerifier.getVerifier().getId());


        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<QualityControl> qcOp  = qcRepository.findById(id);
        if(qcOp.isPresent()){
            QualityControl qc = qcOp.get();
//            demandMailService.prepareMailContent(verificationResponse.getVerifier().getEmployeeName(),"Approval",demand);
//            demandMailService.sentMail(verificationResponse.getVerifier().getEmailAddress(),"Pending Demand Approval Request");
            QcVerifyApprovalHistory qvah = new QcVerifyApprovalHistory();
            qvah.setEmployee(verification.getVerifier());
            qvah.setQcStatus(QcStatus.APPROVED);
            qvah.setQualityControl(qc);
            qcVerifyApprovalHistoryRepository.save(qvah);
            qc.setNextApproverId(nextApprover.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<QualityControl> qcOp = qcRepository.findById(id);
        if (qcOp.isPresent()) {
            QualityControl qc = qcOp.get();

            QcVerifyApprovalHistory qvah = new QcVerifyApprovalHistory();
            qvah.setQcStatus(QcStatus.VERIFIED);
            qvah.setQualityControl(qc);
            qvah.setEmployee(new Employee(qc.getNextVerifierId()));
            qcVerifyApprovalHistoryRepository.save(qvah);
            if (firstApprover.isPresent()) {
                qc.setNextApproverId(firstApprover.get().getVerifier().getId());
                qc.setQcStatus(QcStatus.PENDING_APPROVAL);
            } else {
                qc.setQcStatus(QcStatus.VERIFIED);
            }
        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<QualityControl> qcOp  = qcRepository.findById(id);
        if(qcOp.isPresent()) {
            QualityControl qc = qcOp.get();
            qc.setQcStatus(QcStatus.APPROVED);
        }
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<QualityControl> qcOp  = qcRepository.findById(domainId);
        if(qcOp.isPresent()){
            QualityControl qc = qcOp.get();
            qc.setReviewPrevStatus(qc.getQcStatus());
            qc.setReviewerId(reviewer.getId());
            qc.setQcStatus(QcStatus.REVIEW);
            qc.setReviewDate(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId) {
        Optional<UserApplicationValidation> verificationOp = verificationService
                .getVerificationsByDomainTypeAndDomainIdAndVerifierId(DomainType.QC,domainId,verifier);
        if(verificationOp.isPresent()){
            UserApplicationValidation validation = verificationOp.get();
            validation.setVerified(true);
        }

        Optional<QualityControl> qcOp = qcRepository.findById(domainId);
        if(qcOp.isPresent()){
            QualityControl qc = qcOp.get();
            qc.setQcStatus(QcStatus.REJECTED);
        }
    }

    @Override
    @Transactional
    public void review(Jwt token, Long id, ReviewDto reviewDto) {
        claimResolver.setToken(token);
        Optional<QualityControl> qcOp = qcRepository.findById(id);
        if(qcOp.isEmpty()){
            throw new RuntimeException("QC not found");
        }
        QualityControl qc = qcOp.get();
        qc.setReviewerId(null);
        qc.setQcStatus(qc.getReviewPrevStatus());
        qc.setReviewPrevStatus(null);
        qc.setReviewDate(LocalDateTime.now());

        commentService.addComment(commentService.prepareComment(
                claimResolver.getEmployee().get(),
                reviewDto.getDomainType(),
                reviewDto.getActionType(),
                qc.getId(),
                reviewDto.getMessage(),
                reviewDto.getAttachments()
        ));
    }
}
