package com.agi.aesl.erpscm.demand.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import com.agi.aesl.erpscm.demand.dto.request.DemandReceiveDto;
import com.agi.aesl.erpscm.demand.dto.request.DemandRequestDto;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.demand.dto.response.DemandDetailResDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;

public interface DemandService extends VerificationDomainService{
    Optional<DemandDetailResDto> createDemand(Jwt loggedInUser, String uri, DemandRequestDto demandRequestDto);

    Optional<DemandDetailResDto> updateDemand(Jwt loggedInUser, String uri, Long id, DemandRequestDto demandRequestDto);


    Page<?> getMyDemands(Jwt loggedInUser, Optional<Integer> page, Optional<Integer> size,
                         Optional<String> fromDate, Optional<String> toDate);
    Page<?> getAllDemands(Jwt loggedInUser, Optional<Integer> page, Optional<Integer> size,
                            Optional<String> fromDate, Optional<String> toDate, Optional<Integer> daysRemain
                          );

    Optional<DemandDetailResDto> getDemandDetail(Long id);

    void receiveDemandItem(Jwt loggedInUser, DemandReceiveDto demandReceiveDto);
    void declineDemandItem(Jwt loggedInUser, DemandReceiveDto demandReceiveDto);

    void sentDemandItem(Jwt token, DemandReceiveDto demandReceiveDto);

    Page<?> getAllCloseDemands(Jwt loggedInUser,
                               Optional<Integer> page, Optional<Integer> size,
                               Optional<String> fromDate, Optional<String> toDate
    );

    String getNextDemandNo();

    Page<?> getAllPendingVerificationDemands(Jwt loggedInUser,
                                             Optional<Integer> page, Optional<Integer> size,
                                             Optional<String> fromDate, Optional<String> toDate
                                             );
    Page<?> getAllPendingApprovalDemands(Jwt loggedInUser,
                                         Optional<Integer> page, Optional<Integer> size,
                                         Optional<String> fromDate, Optional<String> toDate

    );

    void reviewDemand(Jwt loggedInUser, Long id, ReviewDto reviewDto);
    void closeDemandItem(Jwt loggedInUser,  DemandReceiveDto demandReceiveDto);
    void rejectDemandItem(Jwt loggedInUser, DemandReceiveDto demandReceiveDto);
    void rejectDemand(Jwt loggedInUser, DemandReceiveDto demandReceiveDto);
    void resendDemandItem(Jwt loggedInUser, DemandReceiveDto demandReceiveDto);
}
