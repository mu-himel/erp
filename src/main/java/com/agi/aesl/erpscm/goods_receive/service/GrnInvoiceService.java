package com.agi.aesl.erpscm.goods_receive.service;

import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface GrnInvoiceService {

    FileUploadResponse uploadInvoice(Jwt token, String id, Optional<MultipartFile>  fileOp);
}
