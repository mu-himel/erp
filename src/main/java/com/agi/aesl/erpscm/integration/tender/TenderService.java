package com.agi.aesl.erpscm.integration.tender;

import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.price_quotation.dto.request.CounterPqDto;
import com.agi.aesl.erpscm.utils.ClaimResolver;

import java.util.Optional;

public interface TenderService {
    Optional<Long> sentCounterOffer(ClaimResolver claimResolver, Indent indent, CounterPqDto counterPqDto);
}
