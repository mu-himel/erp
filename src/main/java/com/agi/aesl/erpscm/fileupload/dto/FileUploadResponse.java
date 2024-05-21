package com.agi.aesl.erpscm.fileupload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResponse {

    String path;
    String size;
    String mimeType;
    String filename;
}
