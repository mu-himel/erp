package com.agi.aesl.erpscm.cs.service;

import com.agi.aesl.erpscm.cs.dto.CsRequestDto;
import com.agi.aesl.erpscm.cs.dto.CsUpdateRequestDto;
import com.agi.aesl.erpscm.cs.enums.CsOperation;
import com.agi.aesl.erpscm.cs.repository.CsRepository;
import com.agi.aesl.erpscm.indent.repository.IndentRepository;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CsService extends VerificationDomainService {
    Page<IndentRepository.CsListInfo> getAllPendingCs(Jwt token,
                                                      Optional<String> indentNo, Optional<String> status,
                                                      Optional<String> fromDateStr, Optional<String> toDateStr,
                                                      Optional<Integer> page, Optional<Integer> size);

    void createCs(Jwt token, String uri, CsRequestDto csRequestDto);
    void updateCsByInitiator(Jwt token, String uri, Long id, CsRequestDto csRequestDto);

    Optional<Map<String,Object>> getDetailById(Long id);

    Optional<CsServiceImpl.CsVendorItemsResult> getAllItemsByVendorAndCs(Long vendorId, String csNo);

    List<CsRepository.ItemWiseVendorDetail> getItemWiseVendors(Long id, String brandName, String itemName);

    Map<String,Object> getItemWiseVendors(Long id, Long vendorId, String itemName);

    void updateCs(Jwt token, Long id, CsUpdateRequestDto csDto, CsOperation csOperation);


    Page<CsRepository.CsPendingListInfo> getPendingVerificationCs(Jwt token,
                                                                  Optional<String> indentNo, Optional<String> status,
                                                                  Optional<String> fromDateStr, Optional<String> toDateStr,
                                                                  Optional<Integer> page, Optional<Integer> size);

    Page<CsRepository.CsPendingListInfo> getPendingApprovalCs(Jwt token,
                                                                 Optional<String> indentNo, Optional<String> status,
                                                                 Optional<String> fromDateStr, Optional<String> toDateStr,
                                                                 Optional<Integer> page, Optional<Integer> size);

    Page<CsRepository.CsPendingListInfo> getApprovedCs(Jwt token,
                                                       Optional<String> indentNo, Optional<String> status,
                                                       Optional<String> fromDateStr, Optional<String> toDateStr,
                                                       Optional<Integer> page, Optional<Integer> size
    );

    Page<CsRepository.CsPendingListInfo> getClosedCs(Jwt loggedInUser,
                                                     Optional<String> indentNo, Optional<String> status,
                                                     Optional<String> fromDateStr, Optional<String> toDateStr,
                                                     Optional<Integer> page, Optional<Integer> size);

    void rejectCs(Jwt loggedInUser, Long id, NoteDto noteDto);

    void reviewCs(Jwt token, Long id, NoteDto noteDto);

    void resentToPr(Jwt token, Long id);

    void resubmit(Jwt token, Long id);
}
