package com.agi.aesl.erpscm.cs.service;

import com.agi.aesl.erpscm.cs.dto.CsRequestDto;
import com.agi.aesl.erpscm.cs.dto.CsUpdateRequestDto;
import com.agi.aesl.erpscm.cs.enums.CsOperation;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CsService extends VerificationDomainService {
    Page<?> getAllPendingCs(Jwt token,
                            Optional<String> indentNo,Optional<String> status,
                            Optional<String> fromDateStr,Optional<String> toDateStr,
                            Optional<Integer> page, Optional<Integer> size);

    void createCs(Jwt token, String uri, CsRequestDto csRequestDto);
    void updateCsByInitiator(Jwt token, String uri, Long id, CsRequestDto csRequestDto);

    Optional<?> getDetailById(Long id);

    Optional<?> getAllItemsByVendorAndCs(Long vendorId, String csNo);

    List<?> getItemWiseVendors(Long id,String brandName, String itemName);

    Map<String,Object> getItemWiseVendors(Long id, Long vendorId, String itemName);

    void updateCs(Jwt token, Long id, CsUpdateRequestDto csDto, CsOperation csOperation);


    Page<?> getPendingVerificationCs(Jwt token,
                                     Optional<String> indentNo, Optional<String> status,
                                     Optional<String> fromDateStr, Optional<String> toDateStr,
                                     Optional<Integer> page, Optional<Integer> size);

    Page<?> getPendingApprovalCs(Jwt token,
                                 Optional<String> indentNo, Optional<String> status,
                                 Optional<String> fromDateStr, Optional<String> toDateStr,
                                 Optional<Integer> page, Optional<Integer> size);

    Page<?> getApprovedCs(Jwt token,
                Optional<String> indentNo, Optional<String> status,
                Optional<String> fromDateStr, Optional<String> toDateStr,
                Optional<Integer> page, Optional<Integer> size
    );

    Page<?> getClosedCs(Jwt loggedInUser,
                        Optional<String> indentNo, Optional<String> status,
                        Optional<String> fromDateStr, Optional<String> toDateStr,
                        Optional<Integer> page, Optional<Integer> size);

    void rejectCs(Jwt loggedInUser, Long id, NoteDto noteDto);

    void reviewCs(Jwt token, Long id, NoteDto noteDto);

    void resentToPr(Jwt token, Long id);

    void resubmit(Jwt token, Long id);
}
