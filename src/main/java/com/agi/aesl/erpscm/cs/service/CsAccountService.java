package com.agi.aesl.erpscm.cs.service;

import com.agi.aesl.erpscm.cs.dto.AcsUpdateDto;
import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.cs.repository.CsAccountRepository;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.Optional;

public interface CsAccountService extends VerificationDomainService {
    void createCsAccount(Cs cs);

    void updateCsAccount(Long id, Jwt token, String uri, AcsUpdateDto acsUpdateDto);

    Page<CsAccountRepository.AcsPendingItem> getPendingAcs(Jwt token,
                                                           Optional<String> indenNo, Optional<String> status,
                                                           Optional<String> fromDateStr, Optional<String> toDateStr,
                                                           Optional<Integer> page, Optional<Integer> size);

    Page<CsAccountRepository.AcsPendingItem> getApprovedAcs(Jwt token,
                           Optional<String> indentNo, Optional<String> status,
                           Optional<String> fromDateStr,Optional<String> toDateStr,
                           Optional<Integer> page, Optional<Integer> size);
    Page<CsAccountRepository.AcsPendingItem> getRejectedAcs(Jwt token,
                           Optional<String> indentNo, Optional<String> status,
                           Optional<String> fromDateStr,Optional<String> toDateStr,
                           Optional<Integer> page, Optional<Integer> size);

    Page<CsAccountRepository.AcsPendingItem> getClosedAcs(Jwt token,
                         Optional<String> indentNo, Optional<String> status,
                         Optional<String> fromDateStr,Optional<String> toDateStr,
                         Optional<Integer> page, Optional<Integer> size);

    void reviewAcs(Jwt token, Long id, NoteDto noteDto);

    Page<CsAccountRepository.AcsPendingItem> getPendingVerificationAcs(Jwt token,
                                      Optional<String> indentNo,  Optional<String> status,
                                      Optional<String> fromDateStr,  Optional<String> toDateStr,
                                      Optional<Integer> page, Optional<Integer> size);

    Page<CsAccountRepository.AcsPendingItem> getPendingApprovalAcs(Jwt token,
                                  Optional<String> indentNo, Optional<String> status,
                                  Optional<String> fromDateStr, Optional<String> toDateStr,
                                  Optional<Integer> page, Optional<Integer> size);
    Page<CsAccountRepository.AcsPendingItem> getActiveCsList(Jwt token, Optional<String> indentNo,
                            Optional<Long> categoryId,
                            Optional<Long> subCategoryId,
                            Optional<String> fromDateStr,
                            Optional<String> toDateStr,
                            Optional<Integer> page, Optional<Integer> size);

    Page<CsAccountRepository.AcsPendingItem> getExpiredCsList(Jwt token, Optional<String> indentNo, Optional<Integer> page, Optional<Integer> size);

    Optional<Map<String,Object>> getDetailById(Long id);
}
