package com.agi.aesl.erpscm.quality_control.service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.enums.QcType;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import com.agi.aesl.erpscm.quality_control.dto.request.QcDto;
import com.agi.aesl.erpscm.quality_control.entity.QualityControl;
import com.agi.aesl.erpscm.quality_control.enums.QcStatus;
import com.agi.aesl.erpscm.quality_control.repository.QcRepository;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ClaimResolver claimResolver;

    @Override
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
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {

    }

    @Override
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {

    }

    @Override
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {

    }

    @Override
    public void approveComplete(Long id) {

    }

    @Override
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {

    }

    @Override
    public void onRejected(Employee verifier, Long domainId) {

    }
}
