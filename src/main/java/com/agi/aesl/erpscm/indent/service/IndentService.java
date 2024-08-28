package com.agi.aesl.erpscm.indent.service;

import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.indent.dto.request.IndentRejectDto;
import com.agi.aesl.erpscm.indent.dto.request.IndentRequestDto;
import com.agi.aesl.erpscm.indent.dto.request.MoveIndentRequestDto;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IndentService extends VerificationDomainService {
    String getNextIndentNo();
    void createIndent(Jwt token, String uri, IndentRequestDto productRequirementRequestDto);

    void updateIndent(Jwt token, String uri, Long id, IndentRequestDto indentRequestDto);
    
    Page<?> getAllIndents(
            Jwt token,
            Optional<Integer> page,
            Optional<Integer> size,
            Optional<Long> categoryId,
            Optional<Long> subCategoryId,
            Optional<String> priority
    );

    Page<?> getAllPendingVerificationIndents(
            Jwt token,
            Optional<Long> categoryId,
            Optional<Long> subCategoryId,
            Optional<String>priority,
            Optional<Integer> page,
            Optional<Integer> size
    );

    Page<?> getAllPendingApprovalIndents(Jwt token,
                                         Optional<Long> categoryId,
                                         Optional<Long> subCategoryId,
                                         Optional<String>priority,
                                         Optional<Integer> page,
                                         Optional<Integer> size);

    Map<String,Object> getIndentDetailById(Long indentId);

    Optional<Indent> getIndentByCode(String code);

    Optional<Indent> getIndentById(Long id);

    List<?> getIndentByIds(Optional<List<Long>> indentIds);

    int moveIndentByIds(MoveIndentRequestDto moveIndent);


    Page<?> getAllClosedIndents(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<Long> categoryId, Optional<Long> subCategoryId, Optional<String> priority);

    void reviewIndent(Jwt token, Long id, ReviewDto reviewDto);
}
