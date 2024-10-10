package com.agi.aesl.erpscm.account_finance.service.bank_account;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository.VerificationResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BankAccServiceImpl implements BankAccService{

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, VerificationResponse nextVerifier) {

    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, VerificationResponse nextApprover) {

    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<VerificationResponse> firstApprover) {

    }

    @Override
    @Transactional
    public void approveComplete(Long id) {

    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {

    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {

    }
}
