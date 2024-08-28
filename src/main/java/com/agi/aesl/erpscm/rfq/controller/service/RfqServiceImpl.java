package com.agi.aesl.erpscm.rfq.controller.service;

import com.agi.aesl.erpscm.indent.repository.IndentRepository;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RfqServiceImpl implements RfqService{

    private static final Integer PAGE_SIZE = 20;
    @Autowired
    private IndentRepository indentRepository;

    @Autowired
    private ClaimResolver claimResolver;

    @Override
    public Page<?> getAllPendingRFQs(Jwt token, Optional<String> indentNo, Optional<String> category,
                                     Optional<String> subCategory, Optional<String> priority,
                                     Optional<Integer> daysRemain, Optional<String> fromDateOp,
                                     Optional<String> toDateOp, Optional<Integer> page, Optional<Integer> size) {

        claimResolver.setToken(token);
        String uri="";

        if((fromDateOp.isPresent() && toDateOp.isEmpty()) ||
                (fromDateOp.isEmpty() && toDateOp.isPresent())){
            throw new RuntimeException("Please select both date filter");
        }
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateOp.isPresent() && toDateOp.isPresent()){
            fromDate = LocalDateTime.parse(fromDateOp.get()+"T00:00:00");
            toDate   = LocalDateTime.parse(fromDateOp.get()+"T23:59:59");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE), sort);

        return indentRepository.getAllApprovedIndents(
                indentNo.orElse(null),
                category.orElse(null),
                subCategory.orElse(null),
                priority.orElse(null),
                daysRemain.orElse(null),
                fromDate,
                toDate,
                pageable);
    }

    @Override
    public Page<?> getAllSentRfqs(Jwt token, Optional<String> indentNo, Optional<String> category,
                                  Optional<String> subCategory, Optional<String> priority, Optional<Integer> daysRemain,
                                  Optional<String> fromDateOp, Optional<String> toDateOp, Optional<Integer> page,
                                  Optional<Integer> size) {

        claimResolver.setToken(token);
        String uri="";

        if((fromDateOp.isPresent() && toDateOp.isEmpty()) ||
                (fromDateOp.isEmpty() && toDateOp.isPresent())){
            throw new RuntimeException("Please select both date filter");
        }
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateOp.isPresent() && toDateOp.isPresent()){
            fromDate = LocalDateTime.parse(fromDateOp.get()+"T00:00:00");
            toDate   = LocalDateTime.parse(fromDateOp.get()+"T23:59:59");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE), sort);
        return indentRepository.getAllIndentsWithOpenRfqStatus(
                indentNo.orElse(null),
                category.orElse(null),
                subCategory.orElse(null),
                priority.orElse(null),
                daysRemain.orElse(null),
                fromDate,
                toDate,
                pageable);
    }
}
