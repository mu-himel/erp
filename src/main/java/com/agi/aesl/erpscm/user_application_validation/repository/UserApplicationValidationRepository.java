package com.agi.aesl.erpscm.user_application_validation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.AssertingParty.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationPanelService;

@Repository
public interface UserApplicationValidationRepository extends JpaRepository<UserApplicationValidation,Long>,VerificationPanelService{

    List<UserApplicationValidation> findByDomainTypeAndDomainId(DomainType domainType, Long id);

    void deleteAllByDomainIdAndDomainType(Long domainId, DomainType domainType);
    interface VerificationResponse{
        Long getId();
        DomainType getDomainType();
        Long getDomainId();
        Employee getVerifier();

        Boolean getVerified();
        Boolean getIsApproval();
        LocalDateTime getVerificationDate();

    }

    @Query(value = "SELECT v FROM UserApplicationValidation v " +
            "LEFT JOIN FETCH v.verifier ve " +
            
            "WHERE v.domainType=:domainType AND v.domainId=:domainId")
    List<VerificationResponse> findAllByDomainTypeAndDomainId(
            @Param("domainType") DomainType domainType,
            @Param("domainId") Long domainId);

    @Query(value = "SELECT v FROM UserApplicationValidation v " +
    "LEFT JOIN FETCH v.verifier ve " +
    "WHERE v.domainType=:domainType AND v.domainId=:domainId " +
    "AND v.verified=:verified AND v.isApproval=:isApproval")
    List<VerificationResponse> findAllByDomainTypeAndDomainIdAndVerifiedAndIsApproval(
        @Param("domainType") DomainType domainType,
            @Param("domainId") Long domainId,
            @Param("verified") Boolean verified,
            @Param("isApproval") Boolean isApproval);

    Optional<UserApplicationValidation> findByDomainTypeAndDomainIdAndVerifierAndIsApproval(
        DomainType domainType, Long domainId,
            Employee verifier, Boolean isApproval);
}
