package com.agi.aesl.erpscm.goods_receive.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.goods_receive.service.GrnInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/upload-invoice")
public class UploadInvoiceController extends BaseController {

    @Autowired
    private GrnInvoiceService grnInvoiceService;

    @PostMapping("/{id}")
    public ResponseEntity<?> uploadInvoice(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") String id,
            @RequestPart("file") Optional<MultipartFile> fileOp
    ){
        return new ResponseEntity<>(grnInvoiceService.uploadInvoice(token,id, fileOp),HttpStatus.OK);
    }

}
