package com.agi.aesl.erpscm.fileupload.service;

import jakarta.servlet.http.HttpServletResponse;

public interface FileDownloadService {
    void downloadFile(String fileUri, HttpServletResponse response);


}
