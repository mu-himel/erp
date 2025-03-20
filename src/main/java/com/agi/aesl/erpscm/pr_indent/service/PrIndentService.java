package com.agi.aesl.erpscm.pr_indent.service;


import com.agi.aesl.erpscm.pr_indent.dto.reqeust.PrIndentRequestDto;
import com.agi.aesl.erpscm.pr_indent.dto.reqeust.UpdatePrIndentDetailRequestDto;
import com.agi.aesl.erpscm.pr_indent.repository.PrIndentRepository;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PrIndentService {

    void createPrIndent(Jwt token, PrIndentRequestDto prIndentRequestDto);

    Page<PrIndentRepository.PrIndentInfo> getAllPrIndents(
            Optional<Integer> page,
            Optional<Integer> size,
            Optional<Long> categoryId,
            Optional<Long> subCategoryId,
            Optional<String> fromDate,
            Optional<String> toDate
    );

    List<Map<String,Object>> getPrIndentById(
            Optional<Long> indentId
    );

    List<Map<String,Object>> getPrIndentByIds(Optional<List<Long>> prIndentIds);

    void updateOrderDetailsOrderQty(UpdatePrIndentDetailRequestDto updateIndentDetailRequestDto );



}