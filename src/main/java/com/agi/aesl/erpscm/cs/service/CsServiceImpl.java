package com.agi.aesl.erpscm.cs.service;

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
public class CsServiceImpl implements CsService{

    private static final Integer PAGE_SIZE = 20;
    @Autowired
    private IndentRepository indentRepository;

    @Autowired
    private ClaimResolver claimResolver;
    @Override
    public Page<?> getAllPendingCs(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE), sort );
        return indentRepository.getAllIndentsByExpireDateTime(LocalDateTime.now(), pageable);
    }
}
