package com.agi.aesl.erpscm.fileupload.service;

import com.agi.aesl.erpscm.goods_receive.exception.FileDownloadException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;

@Slf4j
@Service
public class FileDownloadServiceImpl implements FileDownloadService{


    @Override
    public void downloadFile(String fileUri, HttpServletResponse response) {
        try {

            fileUri = fileUri.trim();
            File file = new File(fileUri);
            log.info("Checking the File exists...");
            if (!file.exists()) {
                throw new FileDownloadException("File not found by the URI : " + fileUri);
            }
            log.info("Processing the File for sending response...");
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            String responseFileName = "inline; filename=\"" + file.getName() + "\"";
            response.setHeader("Content-Disposition", responseFileName);
            response.setContentLength((int) file.length());
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());

        } catch (Exception e) {
            log.info("Downloading file error :: {}", e.getMessage() + "" + e.getCause());
            throw new FileDownloadException(e.getMessage() + "" + e.getCause());
        }
    }


}
