package com.agi.aesl.erpscm.fileupload.service;

import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class FileUploadServiceImpl implements FileUploadService{

    @Override
    public FileUploadResponse uploadFile(Path path, MultipartFile multipartFile) {
        try {
            if(!Files.exists(path)){
                Files.createDirectories(path);
            }
            String filename = multipartFile.getOriginalFilename();
            Path target = path.resolve(filename);
            Files.copy(multipartFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return FileUploadResponse.builder()
                    .filename(filename)
                    .size(String.valueOf(multipartFile.getSize()))
                    .path(path.toString())
                    .mimeType(multipartFile.getContentType())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean validFileSize(Long fileSize, Long limit) {
        return fileSize.equals(limit) || fileSize < limit;
    }

    @Override
    public Boolean checkMimeType(String contentType, String... mimes) {
        List<String> mimeList = List.of(mimes);
        Optional<String> mimeOp = mimeList.stream().filter(mime-> mime.equals(contentType)).findAny();
        return mimeOp.isPresent();
    }
}
