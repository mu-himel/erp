package com.agi.aesl.erpscm.fileupload.service;

import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileUploadService {

    FileUploadResponse uploadFile(Path path, MultipartFile multipartFile);

    Boolean validFileSize(Long fileSize, Long limit);
}
