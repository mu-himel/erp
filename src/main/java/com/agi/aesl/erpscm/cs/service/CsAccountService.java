package com.agi.aesl.erpscm.cs.service;

import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.cs.entity.CsAccount;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;

public interface CsAccountService extends VerificationDomainService {
    void createCsAccount(Cs cs);
}
