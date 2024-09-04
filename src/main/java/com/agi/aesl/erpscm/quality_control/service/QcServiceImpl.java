package com.agi.aesl.erpscm.quality_control.service;

import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccountVerifyApprovalHistory;
import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.demand.service.DemandMailService;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnMode;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.enums.QcType;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import com.agi.aesl.erpscm.modules.service.ModuleService;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.quality_control.dto.request.QcDto;
import com.agi.aesl.erpscm.quality_control.entity.QcVerifyApprovalHistory;
import com.agi.aesl.erpscm.quality_control.entity.QualityControl;
import com.agi.aesl.erpscm.quality_control.entity.QualityControlKpi;
import com.agi.aesl.erpscm.quality_control.enums.QcStatus;
import com.agi.aesl.erpscm.quality_control.repository.QcRepository;
import com.agi.aesl.erpscm.quality_control.repository.QcVerifyApprovalHistoryRepository;
import com.agi.aesl.erpscm.store_receive.enums.SrnStatus;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class QcServiceImpl implements QcService{

    private final Integer MAX_NO_OF_KPI=3;

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
    private QcMailService qcMailService;

    @Autowired
    private OrgService orgService;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private CpsServerConfig cpsServerConfig;

    @Autowired
    private QcVerifyApprovalHistoryRepository qcVerifyApprovalHistoryRepository;

    @Autowired
    private IntegrationReaderService readerService;

    @Override
    @Transactional
    public void addQc(Jwt token, String uri, QcDto controlDto) throws IllegalAccessException {

        claimResolver.setToken(token);
        if(claimResolver.getEmployee().isEmpty()){
            throw new RuntimeException("Store Manager/Executive profile required to perform this");
        }
        AtomicReference<Boolean> error = new AtomicReference<>(false);

        if(controlDto.getKpis()==null || controlDto.getKpis().isEmpty()){
            throw new RuntimeException("Sorry! KPI required");
        }

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

        List<String> ids = new ArrayList<>();
        controlDto.getQcItemDetails().stream().forEach(qcItemDetail -> {
            Optional<GoodReceiveItemDetail> grnItemDetail = grn.getGoodReceiveItemDetails().stream().filter(
                    goodReceiveItemDetail -> goodReceiveItemDetail.getId().equals(qcItemDetail.getId())
            ).findFirst();

            if(grnItemDetail.isPresent()){
                GoodReceiveItemDetail goodReceiveItemDetail = grnItemDetail.get();
                ids.add(goodReceiveItemDetail.getItem().getItemCategory().getId().toString());
                ids.add(goodReceiveItemDetail.getItem().getItemParentCategory().getId().toString());
                goodReceiveItemDetail.setDeclaredQty(qcItemDetail.getDeclaredQty());
                goodReceiveItemDetail.setInspectedQty(qcItemDetail.getInspectedQty());
                goodReceiveItemDetail.setTotalApprovedQty(qcItemDetail.getTotalApproveQty());
                goodReceiveItemDetail.setTotalDeclinedQty(qcItemDetail.getTotalDeclineQty());
                goodReceiveItemDetail.setApproveComment(qcItemDetail.getApproveComment());
                goodReceiveItemDetail.setDeclineComment(qcItemDetail.getDeclineComment());
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
//        if(controlDto.getQcStatus() == QcStatus.PARTIALLY_APPROVED ||
//                controlDto.getQcStatus() == QcStatus.APPROVED) {
//
            qualityControl.setQcStatus(controlDto.getQcStatus());
            qualityControl.setStatus(controlDto.getQcStatus().toString());

            qualityControl.setQualityControlKpis(
                    controlDto.getKpis().stream().map(qualityControlKpi -> {
                        qualityControlKpi.setQualityControl(qualityControl);
                        if (qualityControlKpi.getQcType() == QcType.PASS) {
                            qcPassCount.getAndSet(qcPassCount.get() + 1);
                        }
                        return qualityControlKpi;
                    }).collect(Collectors.toList())
            );

//        }else{
//
//            qualityControl.setQcStatus(QcStatus.REJECTED);
//        }

        qcRepository.save(qualityControl);
        if(qualityControl.getQcStatus().equals(QcStatus.PARTIALLY_APPROVED)){
            grn.setGrnStatus(GrnStatus.QC_PARTIAL);
        }
        if(qualityControl.getQcStatus().equals(QcStatus.APPROVED)){
            grn.setGrnStatus(GrnStatus.QC_PASS);
        }
        if(qualityControl.getQcStatus().equals(QcStatus.REJECTED)){
            grn.setGrnStatus(GrnStatus.QC_FAILED);
        }

//        if(qcPassCount.get().equals(MAX_NO_OF_KPI)){
//            grn.setGrnStatus(GrnStatus.READY_FOR_STORE);
//        }
//        if(qualityControl.getQcStatus().equals(QcStatus.PARTIALLY_APPROVED)){
//            grn.setGrnStatus(GrnStatus.QC_PARTIAL);
//        }
//        if(qualityControl.getQcStatus().equals(QcStatus.REJECTED) ||  qcFailCount.get()>0){
//            grn.setGrnStatus(GrnStatus.QC_FAILED);
//        }
//        if(qcPassCount.get()==0 && qcFailCount.get()==0 ){
//            grn.setGrnStatus(GrnStatus.QC_HOLD);
//        }

        if(ids.size()>0 && !uri.isBlank() && !qualityControl.getQcStatus().equals(QcStatus.REJECTED)) {
            System.out.println(uri);
            AppliedVADto result = verificationService.applyVerifyApprovalProcess(qualityControl, DomainType.QC, QcStatus.APPROVED.toString(), uri, "CATEGORY", ids,
                    null);

            if(result.getVerifiers().isEmpty() && result.getPanels().isEmpty()){

                controlDto.getQcItemDetails().stream().forEach(qcItemDetail -> {
                    Optional<GoodReceiveItemDetail> grnItemDetail = grn.getGoodReceiveItemDetails().stream().filter(
                            goodReceiveItemDetail -> goodReceiveItemDetail.getId().equals(qcItemDetail.getId())
                    ).findFirst();

                    if(grnItemDetail.isPresent()){
                        GoodReceiveItemDetail goodReceiveItemDetail = grnItemDetail.get();
//                        ids.add(goodReceiveItemDetail.getItem().getItemCategory().getId().toString());
//                        ids.add(goodReceiveItemDetail.getItem().getItemParentCategory().getId().toString());
//                        goodReceiveItemDetail.setDeclaredQty(qcItemDetail.getDeclaredQty());
//                        goodReceiveItemDetail.setInspectedQty(qcItemDetail.getInspectedQty());
                        goodReceiveItemDetail.setTotalApprovedQty(qcItemDetail.getDeclaredQty());
                        grnService.updateGrnItemDetail(goodReceiveItemDetail);
                    }
                });
                qualityControl.setQcStatus(QcStatus.COMPLETED);
                grn.setGrnStatus(GrnStatus.READY_FOR_STORE);
            }

        }
    }

    @Override
    public Optional<?> getDetailByGrnId(Long id) {
        Optional<?> detailOp = grnService.getGrnById(id,false);
        if(detailOp.isPresent()){
            Map<String,Object> detail = (Map<String,Object>)detailOp.get();
            List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                    .getVerificationsByDomainTypeAndDomainId(DomainType.QC, (Long)detail.get("qcId"));
            vrs.stream().forEach(verifier->{
                if(verifier.getIsApproval()==false){
                    verifiers.add(verifier);
                }else{
                    approvers.add(verifier);
                }
            });

            List<?> comments = commentService.getCommentsByDomain(DomainType.QC, (Long)detail.get("qcId"));


            detail.put("approvers",approvers);
            detail.put("comments",comments);
            detail.put("verifiers",verifiers);
            return Optional.ofNullable(detail);
        }
        return detailOp;
    }

    @Override
    public Optional<?> getByGrnId(Long id) {
        return grnService.getGRNById(id, true);
    }

    @Override
    public List<?> getQcResultByGrn(Long id) {
        return qcRepository.getQcResultByGrn(id);
    }

    @Override
    @Transactional
    public void rejectQc(Jwt token, Long id, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<GoodReceiveNote> goodReceiveNoteOptional = (Optional<GoodReceiveNote>)grnService.getGRNById(id, true);
        if(goodReceiveNoteOptional.isPresent()){
            GoodReceiveNote grn = goodReceiveNoteOptional.get();
            grn.setGrnStatus(GrnStatus.REJECTED);

            List<QualityControlKpi> kpis =  new ArrayList<>();

            QualityControl qc = new QualityControl();
            qc.setComment(noteDto.getNote());
            Optional<Employee> empOp = claimResolver.getEmployee();
            if(empOp.isPresent()) {
                Employee employee = empOp.get();
                qc.setCreatedBy(new Employee(employee.getId()));
                qc.setWarehouse(new Warehouse(employee.getWarehouseId()));
            }
            qc.setGoodReceiveNote(grn);
            qc.setQcDate(LocalDate.now());
            qc.setQcStatus(QcStatus.REJECTED);

            kpis.add(getKpi("Description Goods",qc));
            kpis.add(getKpi("Quantity",qc));
            kpis.add(getKpi("Quality",qc));
            kpis.add(getKpi("Packing & Labeling",qc));
            qc.setQualityControlKpis(kpis);
            qcRepository.save(qc);
            ObjectMapper mapper = new ObjectMapper();
            String kpi = null;
            try {
                kpi = mapper.writeValueAsString(kpis);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            if(grn.getGrnMode().equals(GrnMode.AUTO)) {
                sentQcStatus(token.getTokenValue(), grn.getRemotePoId(), GrnStatus.QC_FAILED, new ArrayList<>(), noteDto, kpi);
            }
        }
    }

    @Transactional
    private void sentQcStatus(String token, Long id,GrnStatus status, List<?> qcDetails, NoteDto noteDto, String qcResult){
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token);
        if(orgOp.isPresent()){
            headers.set("orgId",orgOp.get().getCpsVendorRegistrationId().toString());
        }

        Map<String,Object> map = new HashMap<>();
        map.put("note",noteDto.getNote());
        map.put("status",status);
        map.put("qcDetails",qcDetails);
        map.put("qcResult",qcResult);
        HttpEntity<Map<String,Object>> payload = new HttpEntity<>(map,headers);
        String url = (status.equals(GrnStatus.QC_PASS))? cpsServerConfig.getPoQcPassEndpoint(id):
                cpsServerConfig.getPoQcFailEndpoint(id);
        ResponseEntity<?> response = networkService.put(url, payload, Void.class);
        if(response.getStatusCode()!= HttpStatus.NO_CONTENT){
            throw new RuntimeException("Sorry! Something wrong");
        }

    }

    private QualityControlKpi getKpi(String name, QualityControl qc){
        QualityControlKpi qck  = new QualityControlKpi();
        qck.setName(name);
        qck.setQcType(QcType.FAIL);
        qck.setRemark("REJECTED");
        qck.setQualityControl(qc);
        return qck;
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
            if(!qc.getQcStatus().equals(QcStatus.REVIEW)) {
                qc.setReviewPrevStatus(qc.getQcStatus());
                qc.setQcStatus(QcStatus.REVIEW);
            }
            qc.setReviewerId(reviewer.getId());
            qc.setReviewDate(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {
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


            if(qc.getGoodReceiveNote()!=null){
                GoodReceiveNote grn = qc.getGoodReceiveNote();
                grn.setGrnStatus(GrnStatus.REJECTED);

                List<QualityControlKpi> kpis =  new ArrayList<>();


                qc.setComment("Rejected");
                Optional<Employee> empOp = claimResolver.getEmployee();
                if(empOp.isPresent()) {
                    Employee employee = empOp.get();
                    qc.setCreatedBy(new Employee(employee.getId()));
                    qc.setWarehouse(new Warehouse(employee.getWarehouseId()));
                }
                qc.setGoodReceiveNote(grn);
                qc.setQcDate(LocalDate.now());
                qc.setQcStatus(QcStatus.REJECTED);

                kpis.add(getKpi("Description Goods",qc));
                kpis.add(getKpi("Quantity",qc));
                kpis.add(getKpi("Quality",qc));
                kpis.add(getKpi("Packing & Labeling",qc));
                qc.setQualityControlKpis(kpis);
                qcRepository.save(qc);
                ObjectMapper mapper = new ObjectMapper();
                String kpi = null;
                try {
                    kpi = mapper.writeValueAsString(kpis);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                if(grn.getGrnMode().equals(GrnMode.AUTO)) {
                    NoteDto note = new NoteDto(rejectDto.getComment());
                    sentQcStatus(claimResolver.getToken().getTokenValue(), grn.getRemotePoId(), GrnStatus.QC_FAILED,
                            new ArrayList<>(), note, kpi);
                }
            }
        }else{
            throw new RuntimeException("QC not found");
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
        if(qc.getReviewPrevStatus()!=null) {
            qc.setQcStatus(qc.getReviewPrevStatus());
        }
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

    @Override
    public Page<?> getAllPendingVerificationQC(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                               Optional<String> grnNo, Optional<Integer> qty,
                                               Optional<Integer> receivedQty, Optional<String> fromDate,
                                               Optional<String> toDate) {
        claimResolver.setToken(token);

        String uri="inventory-management/good-receive/quality-check-pending-verification";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + "T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get() + "T23:59:59");
        }

        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(readerService);
        List<Long> warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        List<Long> categoryIds = dataFilter.getCategoryIds();
        List<String> status = new ArrayList<>();
        status.add(QcStatus.PENDING_VERIFICATION.toString());
        status.add(QcStatus.VERIFIED.toString());
        status.add(QcStatus.REVIEW.toString());

        return qcRepository.findAllPendingVerification(
                    warehouseIds,categoryIds, claimResolver.getUserId(),status,
                    grnNo.orElse(null),
                    qty.orElse(null),
                    receivedQty.orElse(null),
                    fromDateObj,
                    toDateObj,
                    pageable
                );
    }

    @Override
    public Page<?> getAllPendingApprovalQC(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<String> grnNo, Optional<Integer> qty, Optional<Integer> receivedQty, Optional<String> fromDate, Optional<String> toDate) {
        claimResolver.setToken(token);

        String uri="inventory-management/good-receive/quality-check-pending-approval";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + "T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get() + "T23:59:59");
        }

        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(readerService);
        List<Long> warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        List<Long> categoryIds = dataFilter.getCategoryIds();
        List<String> status = new ArrayList<>();
        status.add(SrnStatus.PENDING_APPROVAL.toString());
        status.add(SrnStatus.APPROVED.toString());
        status.add(SrnStatus.REVIEW.toString());
        return qcRepository.findAllPendingApproval(
                warehouseIds,categoryIds, claimResolver.getUserId(),
                grnNo.orElse(null),
                qty.orElse(null),
                receivedQty.orElse(null),
                status,
                fromDateObj,
                toDateObj,
                pageable
        );
    }

    @Override
    public Page<?> getAllClosed(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                Optional<String> grnNo, Optional<Integer> qty, Optional<Integer> receivedQty,
                                Optional<String> fromDate, Optional<String> toDate) {
        claimResolver.setToken(token);

        String uri="inventory-management/good-receive/quality-check-closed";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + "T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get() + "T23:59:59");
        }

        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(readerService);
        List<Long> warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        List<Long> categoryIds = dataFilter.getCategoryIds();

        return qcRepository.findAllClosed(
                warehouseIds,categoryIds,
                grnNo.orElse(null),
                qty.orElse(null),
                receivedQty.orElse(null),
                fromDateObj,
                toDateObj,
                pageable
        );
    }

    @Override
    public Page<?> getAllRejected(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<String> grnNo,
                                  Optional<Integer> qty, Optional<Integer> receivedQty, Optional<String> fromDate,
                                  Optional<String> toDate) {

        claimResolver.setToken(token);
        String uri="inventory-management/good-receive/quality-check-rejected";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + "T00:00:00");
            toDateObj = LocalDateTime.parse(toDate.get() + "T23:59:59");
        }

        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(readerService);
        List<Long> warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        List<Long> categoryIds = dataFilter.getCategoryIds();

        return qcRepository.findAllRejected(
                warehouseIds,categoryIds,
                grnNo.orElse(null),
                qty.orElse(null),
                receivedQty.orElse(null),
                fromDateObj,
                toDateObj,
                pageable
        );
    }
}
