package com.agi.aesl.erpscm.goods_receive.service;

import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import com.agi.aesl.erpscm.fileupload.service.FileDownloadService;
import com.agi.aesl.erpscm.fileupload.service.FileUploadService;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.exception.FileDownloadException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class GrnInvoiceServiceImpl implements GrnInvoiceService{

    private final FileUploadService fileUploadService;
    private final FileDownloadService fileDownloadService;

    private final GrnService grnService;

    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    @Transactional
    public FileUploadResponse uploadInvoice(Jwt token, String id, Optional<MultipartFile> fileOp) {

        Optional<GoodReceiveNote> goodReceiveNoteOp = (Optional<GoodReceiveNote>) grnService.getByGrnNo(id);
        if(goodReceiveNoteOp.isEmpty()){
            throw new RuntimeException("Sorry! Grn not found");
        }

        GoodReceiveNote goodReceiveNote = goodReceiveNoteOp.get();

        if(fileOp.isPresent()){
            MultipartFile file = fileOp.get();

            if(!fileUploadService.validFileSize(file.getSize(), Long.valueOf(5L*(1024*1024)))){
                throw new RuntimeException("Sorry! Valid file size upto 5M");
            }


            Path path = Path.of(uploadDir+"/grn/"+id+"/po/invoice");

            FileUploadResponse fileUploadResponse = fileUploadService.uploadFile(path, file);
            if(fileUploadResponse!=null){
                goodReceiveNote.setInvoicePath(path.resolve(fileUploadResponse.getFilename()).toString());
                return fileUploadResponse;
            }
        }

        return null;
    }

    @Override
    public void downloadInvoice(Optional<String> fileUriOp, HttpServletResponse response  ) {
        if ( fileUriOp.isEmpty()) {
            log.info(" Header \"File_Uri\" not provided.");
            throw new FileDownloadException(" Header \"File_Uri\" not provided.");
        }

         fileDownloadService.downloadFile(fileUriOp.get(), response);

    }
}
