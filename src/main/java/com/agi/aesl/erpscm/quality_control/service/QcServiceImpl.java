package com.agi.aesl.erpscm.quality_control.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnMode;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.enums.QcType;
import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
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
import com.agi.aesl.erpscm.quality_control.repository.QcQuery;
import com.agi.aesl.erpscm.quality_control.repository.QcRepository;
import com.agi.aesl.erpscm.quality_control.repository.QcVerifyApprovalHistoryRepository;
import com.agi.aesl.erpscm.store_receive.enums.SrnStatus;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

@Service
@RequiredArgsConstructor
public class QcServiceImpl implements QcService{

    private static final Integer MAX_NO_OF_KPI=3;


    private final QcRepository qcRepository;


    @Setter
    private  GrnService grnService;


    private final UserApplicationValidatorService<QualityControl> verificationService;

    private final CommentService commentService;


    private final ModuleService moduleService;


    private final ClaimResolver claimResolver;


    private final QcMailService qcMailService;


    private final OrgService orgService;


    private final NetworkService networkService;


    private final CpsServerConfig cpsServerConfig;


    private final QcVerifyApprovalHistoryRepository qcVerifyApprovalHistoryRepository;


    private final IntegrationReaderService readerService;

    private final QcSentService qcSentService;

    private static final String DATE_TIME_START="T00:00:00";
    private static final String DATE_TIME_END="T23:59:59";

    private Employee getEmp(){
        return claimResolver.getEmployee().orElse(null);
    }

    private Long getEmpWarehouseId(){
        Employee emp = claimResolver.getEmployee().orElse(null);
        return (emp!=null)? emp.getWarehouseId() : null;
    }

    @Override
    @Transactional
    public void addQc(Jwt token, String uri, QcDto controlDto) throws IllegalAccessException {

        claimResolver.setToken(token);
        if(claimResolver.getEmployee().isEmpty()){
            throw new AesException("Store Manager/Executive profile required to perform this");
        }
        AtomicReference<Boolean> error = new AtomicReference<>(false);

        if(controlDto.getKpis()==null || controlDto.getKpis().isEmpty()){
            throw new AesException("Sorry! KPI required");
        }

        controlDto.getKpis().forEach(qualityControlKpi -> {
            if(qualityControlKpi.getQcType()!=null){
                error.set(true);
            }
        });
        if(Boolean.TRUE.equals(error.get())){
            throw new AesException("Inspection summary should be submit for all kpi");
        }

        if(controlDto.getGrn()==null || controlDto.getGrn().getId() == null){
            throw new AesException("Good Receive Note Reference missing");
        }

        Optional<GoodReceiveNote> goodReceiveNoteOptional = grnService.getGrn(controlDto.getGrn().getId(),true);
        if(goodReceiveNoteOptional.isEmpty()){
            throw new AesException("Good Receive note not found");
        }

        GoodReceiveNote grn = goodReceiveNoteOptional.get();

        List<String> ids = new ArrayList<>();
        controlDto.getQcItemDetails().forEach(qcItemDetail -> {
            Optional<GoodReceiveItemDetail> grnItemDetail = grn.getGoodReceiveItemDetails().stream().filter(
                    goodReceiveItemDetail -> goodReceiveItemDetail.getId().equals(qcItemDetail.getId())
            ).findFirst();

            if(qcItemDetail.getDeclaredQty()==null){
                throw new AesException("Sorry! Declared Qty Required");
            }

            if(qcItemDetail.getInspectedQty()==null){
                throw new AesException("Sorry! Inspected Qty Required");
            }
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
        claimResolver.getEmployee().ifPresent(qualityControl::setCreatedBy);
        claimResolver.getEmployee().ifPresent(emp->qualityControl.setWarehouse(new Warehouse(emp.getWarehouseId())));
        qualityControl.setGoodReceiveNote(grn);

        AtomicReference<Integer> qcPassCount = new AtomicReference<>(0);

            qualityControl.setQcStatus(controlDto.getQcStatus());
            qualityControl.setStatus(controlDto.getQcStatus().toString());

            qualityControl.setQualityControlKpis(
                    controlDto.getKpis().stream().map(qualityControlKpi -> {
                        qualityControlKpi.setQualityControl(qualityControl);
                        if (qualityControlKpi.getQcType() == QcType.PASS) {
                            qcPassCount.getAndSet(qcPassCount.get() + 1);
                        }
                        return qualityControlKpi;
                    }).toList()
            );


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

        if(!ids.isEmpty() && !uri.isBlank() && !qualityControl.getQcStatus().equals(QcStatus.REJECTED)) {

            AppliedVADto result = verificationService.applyVerifyApprovalProcess(qualityControl, DomainType.QC, QcStatus.APPROVED.toString(), uri, "CATEGORY", ids,
                    null);

            if(result.getVerifiers().isEmpty() && result.getPanels().isEmpty()){

                controlDto.getQcItemDetails().forEach(qcItemDetail -> {
                    Optional<GoodReceiveItemDetail> grnItemDetail = grn.getGoodReceiveItemDetails().stream().filter(
                            goodReceiveItemDetail -> goodReceiveItemDetail.getId().equals(qcItemDetail.getId())
                    ).findFirst();

                    if(grnItemDetail.isPresent()){
                        GoodReceiveItemDetail goodReceiveItemDetail = grnItemDetail.get();
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
    public Optional<Map<String,Object>> getDetailByGrnId(Long id) {
        Optional<Map<String, Object>> detailOp = grnService.getGrnById(id,false);
        if(detailOp.isPresent()){
            Map<String,Object> detail = detailOp.get();
            List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                    .getVerificationsByDomainTypeAndDomainId(DomainType.QC, (Long)detail.get("qcId"));
            vrs.forEach(verifier->{
                if(Boolean.FALSE.equals(verifier.getIsApproval())){
                    verifiers.add(verifier);
                }else{
                    approvers.add(verifier);
                }
            });

            List<?> comments = commentService.getCommentsByDomain(DomainType.QC, (Long)detail.get("qcId"));


            detail.put("approvers",approvers);
            detail.put("comments",comments);
            detail.put("verifiers",verifiers);
            return Optional.of(detail);
        }
        return Optional.empty();
    }


    @Override
    public List<QcQuery.QcResultItem> getQcResultByGrn(Long id) {
        return qcRepository.getQcResultByGrn(id);
    }

    @Override
    @Transactional
    public void rejectQc(Jwt token, Long id, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<GoodReceiveNote> goodReceiveNoteOptional = grnService.getGrn(id, true);
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
                throw new AesException(e.getMessage());
            }
            if(grn.getGrnMode().equals(GrnMode.AUTO)) {
                qcSentService.setOrgService(orgService);
                qcSentService.setCpsServerConfig(cpsServerConfig);
                qcSentService.setNetworkService(networkService);
                qcSentService.sentQcStatus(token.getTokenValue(), grn.getRemotePoId(), GrnStatus.QC_FAILED, new ArrayList<>(), noteDto, kpi);
            }
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
                qc.getGoodReceiveNote().setGrnStatus(GrnStatus.READY_FOR_STORE);
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
            qc.getGoodReceiveNote().setGrnStatus(GrnStatus.READY_FOR_STORE);

            QcVerifyApprovalHistory qvah = new QcVerifyApprovalHistory();
            qvah.setQcStatus(QcStatus.APPROVED);
            qvah.setQualityControl(qc);
            qvah.setEmployee(new Employee(qc.getNextApproverId()));
            qcVerifyApprovalHistoryRepository.save(qvah);
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
                    throw new AesException(e.getMessage());
                }
                if(grn.getGrnMode().equals(GrnMode.AUTO)) {
                    NoteDto note = new NoteDto(rejectDto.getComment());
                    qcSentService.setOrgService(orgService);
                    qcSentService.setCpsServerConfig(cpsServerConfig);
                    qcSentService.setNetworkService(networkService);
                    qcSentService.sentQcStatus(claimResolver.getToken().getTokenValue(), grn.getRemotePoId(), GrnStatus.QC_FAILED,
                            new ArrayList<>(), note, kpi);
                }
            }
        }else{
            throw new AesException("QC not found");
        }

    }

    @Override
    @Transactional
    public void review(Jwt token, Long id, ReviewDto reviewDto) {
        claimResolver.setToken(token);
        Optional<QualityControl> qcOp = qcRepository.findById(id);
        if(qcOp.isEmpty()){
            throw new AesException("QC not found");
        }
        QualityControl qc = qcOp.get();
        qc.setReviewerId(null);
        if(qc.getReviewPrevStatus()!=null) {
            qc.setQcStatus(qc.getReviewPrevStatus());
        }
        qc.setReviewPrevStatus(null);
        qc.setReviewDate(LocalDateTime.now());

        commentService.addComment(commentService.prepareComment(
                getEmp(),
                reviewDto.getDomainType(),
                reviewDto.getActionType(),
                qc.getId(),
                reviewDto.getMessage(),
                reviewDto.getAttachments()
        ));
    }

    @Override
    public Page<GrnRepository.GoodReceiveNoteInfo> getAllPendingVerificationQC(Jwt token, Optional<Integer> page, Optional<Integer> size,
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
            fromDateObj = LocalDateTime.parse(fromDate.get() + DATE_TIME_START);
            toDateObj = LocalDateTime.parse(toDate.get() + DATE_TIME_END);
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
    public Page<GrnRepository.GoodReceiveNoteInfo> getAllPendingApprovalQC(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<String> grnNo, Optional<Integer> qty, Optional<Integer> receivedQty, Optional<String> fromDate, Optional<String> toDate) {
        claimResolver.setToken(token);

        String uri="inventory-management/good-receive/quality-check-pending-approval";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + DATE_TIME_START);
            toDateObj = LocalDateTime.parse(toDate.get() + DATE_TIME_END);
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
    public Page<GrnRepository.GoodReceiveNoteInfo> getAllClosed(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                                Optional<String> grnNo, Optional<Integer> qty, Optional<Integer> receivedQty,
                                                                Optional<String> fromDate, Optional<String> toDate) {
        claimResolver.setToken(token);
        if(claimResolver.getEmployee().isEmpty()){
            throw new AesException("Sorry! Qc Relevant Employee Profile Required");
        }
        String uri="inventory-management/good-receive/quality-check-closed";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + DATE_TIME_START);
            toDateObj = LocalDateTime.parse(toDate.get() + DATE_TIME_END);
        }

        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(readerService);
        List<Long> warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        List<Long> categoryIds = dataFilter.getCategoryIds();
        if(getEmpWarehouseId()!=null) {
            warehouseIds.add(getEmpWarehouseId());
        }
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
    public Page<GrnRepository.GoodReceiveNoteInfo> getAllRejected(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<String> grnNo,
                                                                  Optional<Integer> qty, Optional<Integer> receivedQty, Optional<String> fromDate,
                                                                  Optional<String> toDate) {

        claimResolver.setToken(token);
        String uri="inventory-management/good-receive/quality-check-rejected";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        LocalDateTime fromDateObj = null;
        LocalDateTime toDateObj = null;

        if(fromDate.isPresent() && toDate.isPresent()) {
            fromDateObj = LocalDateTime.parse(fromDate.get() + DATE_TIME_START);
            toDateObj = LocalDateTime.parse(toDate.get() + DATE_TIME_END);
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
