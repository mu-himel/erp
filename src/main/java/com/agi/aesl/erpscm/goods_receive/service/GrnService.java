package com.agi.aesl.erpscm.goods_receive.service;

import com.agi.aesl.erpscm.goods_receive.dto.request.GoodReceiveNoteDto;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface GrnService {

    String getNextGrnNumber();

    void addGrn(Jwt token, GoodReceiveNoteDto goodReceiveNoteDto);

    Page<?> getAllGrn(Optional<Integer> page, Optional<Integer> size, Optional<String> fromDate, Optional<String> toDate);

    Optional<?> getGrnById(Long id, Boolean returnTypeEntity);
}
