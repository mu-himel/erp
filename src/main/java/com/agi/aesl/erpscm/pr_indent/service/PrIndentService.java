package com.agi.aesl.erpscm.pr_indent.service;


import com.agi.aesl.erpscm.pr_indent.dto.reqeust.PrIndentRequestDto;
import com.agi.aesl.erpscm.pr_indent.dto.reqeust.UpdatePrIndentDetailRequestDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface PrIndentService {

    void createPrIndent(PrIndentRequestDto prIndentRequestDto);

    Page<?> getAllPrIndents(
            Optional<Integer> page,
            Optional<Integer> size,
            Optional<Long> categoryId,
            Optional<Long> subCategoryId,
            Optional<String> priority
    );

    List<?> getPrIndentById(
            Optional<Long> indentId
    );

    List<?> getPrIndentByIds(Optional<List<Long>> prIndentIds);

    void updateOrderDetailsOrderQty(UpdatePrIndentDetailRequestDto updateIndentDetailRequestDto );



}