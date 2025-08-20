/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomMultipartFile implements MultipartFile {
	private final byte[] fileBytes;
    private final String fileName;
    private final String contentType;

    public CustomMultipartFile(byte[] fileBytes, String fileName, String contentType) {
        this.fileBytes = fileBytes;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return fileBytes == null || fileBytes.length == 0;
    }

    @Override
    public long getSize() {
        return fileBytes.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return fileBytes;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(fileBytes);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException {
        // Implement if needed IllegalStateException
    }
}