package com.agi.aesl.erpscm.goods_receive.service;

import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import com.agi.aesl.erpscm.fileupload.service.FileUploadService;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Optional;

@Service
public class GrnInvoiceServiceImpl implements GrnInvoiceService{

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private GrnService grnService;

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
}
