package com.agi.aesl.erpscm.cs.service;

import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.cs.entity.CsAccount;
import com.agi.aesl.erpscm.cs.enums.CsStatus;
import com.agi.aesl.erpscm.cs.repository.CsAccountRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CsAccountServiceImpl implements CsAccountService{

    @Autowired
    private CsAccountRepository csAccountRepository;

    @Override
    @Transactional
    public void createCsAccount(Cs cs) {
        CsAccount csAccount = new CsAccount();
        csAccount.setCs(cs);
        csAccount.setAcsStatus(CsStatus.PENDING);
        csAccountRepository.save(csAccount);
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<CsAccount> csAccOp = csAccountRepository.findById(id);
        if(csAccOp.isPresent()){
            CsAccount cs = csAccOp.get();
            cs.setNextVerifierId(nextVerifier.getVerifier().getId());
            cs.setAcsStatus(CsStatus.VERIFIED);
//            setVAHistory(cs, CsStatus.VERIFIED);
        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<CsAccount> csAccountOp  = csAccountRepository.findById(id);
        if(csAccountOp.isPresent()){
            CsAccount csAccount = csAccountOp.get();
            csAccount.setNextApproverId(nextApprover.getVerifier().getId());
            csAccount.setAcsStatus(CsStatus.APPROVED);
//            setVAHistory(cs, CsStatus.APPROVED);
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<CsAccount> csAccountOp  = csAccountRepository.findById(id);
        if(csAccountOp.isPresent()){
            CsAccount csAccount = csAccountOp.get();
            if(firstApprover.isPresent()){
                csAccount.setNextApproverId(firstApprover.get().getVerifier().getId());
                csAccount.setAcsStatus(CsStatus.PENDING_APPROVAL);
//                setVAHistory(cs,CsStatus.VERIFIED);

            }else {

                csAccount.setAcsStatus(CsStatus.VERIFIED);
//                setVAHistory(cs,CsStatus.VERIFIED);
            }

        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<CsAccount> csAccountOp  = csAccountRepository.findById(id);
        if(csAccountOp.isPresent()){
            CsAccount csAccount = csAccountOp.get();
            csAccount.setAcsStatus(CsStatus.APPROVED);
//            setVAHistory(cs,CsStatus.APPROVED);

        }
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<CsAccount> csAccountOp  = csAccountRepository.findById(domainId);
        if(csAccountOp.isPresent()){
            CsAccount csAccount = csAccountOp.get();
            csAccount.setReviewerId(reviewer.getId());
            csAccount.setReviewPrevStatus(csAccount.getAcsStatus());
            csAccount.setAcsStatus(CsStatus.REVIEW);

        }
    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {

    }
}
