package com.agi.aesl.erpscm.goods_receive.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.goods_receive.service.GrnInvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/download-invoice")
public class DownloadInvoiceController extends BaseController {


    private final GrnInvoiceService grnInvoiceService;


    @GetMapping
    public void downloadInvoice(
            @RequestHeader("file_uri") Optional<String> fileUriOp,
            HttpServletResponse response
    )  {
        log.info("calling /api/v1/download-invoice");
        grnInvoiceService.downloadInvoice(fileUriOp, response);
    }

}
