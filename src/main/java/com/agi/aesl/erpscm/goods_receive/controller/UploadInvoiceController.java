package com.agi.aesl.erpscm.goods_receive.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.goods_receive.service.GrnInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/upload-invoice")
@RequiredArgsConstructor
public class UploadInvoiceController extends BaseController {


    private final GrnInvoiceService grnInvoiceService;

    @PostMapping("/{id}")
    public ResponseEntity<Object> uploadInvoice(
            @AuthenticationPrincipal Jwt token,
            @PathVariable("id") String id,
            @RequestPart("file") Optional<MultipartFile> fileOp
    ){
        return new ResponseEntity<>(grnInvoiceService.uploadInvoice(token,id, fileOp),HttpStatus.OK);
    }

}
