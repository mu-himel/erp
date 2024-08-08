package com.agi.aesl.erpscm.goods_receive.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface GrnInvoiceService {

    void uploadInvoice(Jwt token, Long id, Optional<MultipartFile>  fileOp);
}
