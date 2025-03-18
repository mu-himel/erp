package com.agi.aesl.erpscm.store_receive.service;

import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.store_receive.dto.SrnDto;
import com.agi.aesl.erpscm.store_receive.repository.SrnRepository;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SrnService extends VerificationDomainService {
    void addSrn(Jwt token, SrnDto srnDto);

    List<SrnRepository.PendingDemandList> getPendingDemandListBySrnItems(Long id);
    List<SrnRepository.PendingDemandList> getPendingDemandListBySrnItems(Jwt token, String attributes);

    Page<SrnRepository.StoreReceiveNoteInfo> getAll(Jwt token, Pageable pageable, Optional<String> grnNo,
                                                    Optional<Long> categoryId, Optional<Long> receivedQty,
                                                    Optional<String> fromDate, Optional<String> toDate);
    Page<SrnRepository.StoreReceiveNoteInfo> getAllComplete(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<String> grnNo,
                                                            Optional<String> fromDate, Optional<String> toDate);

    void review(Jwt token, Long id, ReviewDto reviewDto);

    Page<SrnRepository.StoreReceiveNoteInfo> getPendingVerifications(Jwt token, Optional<String> fromDate, Optional<String> toDate,
                                                                     Optional<Integer> page, Optional<Integer> size);

    Page<SrnRepository.StoreReceiveNoteInfo> getPendingApprovals(Jwt token, Optional<String> fromDate,
                                                                 Optional<String> toDate, Optional<Integer> page, Optional<Integer> size);

    Optional<Map<String,Object>> getDetail(Long id);

}
