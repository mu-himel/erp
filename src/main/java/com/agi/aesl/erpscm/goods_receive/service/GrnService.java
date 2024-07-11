package com.agi.aesl.erpscm.goods_receive.service;

import com.agi.aesl.erpscm.goods_receive.dto.request.GoodReceiveNoteDto;
import org.springframework.security.oauth2.jwt.Jwt;

public interface GrnService {

    String getNextGrnNumber();

    void addGrn(Jwt token, GoodReceiveNoteDto goodReceiveNoteDto);
}
