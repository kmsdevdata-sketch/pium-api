package com.pium.application.skinanalysis.image.dto;

import java.util.Arrays;

public record SkinImageFile(
        String originalFilename,
        String contentType,
        long size,
        byte[] bytes
) {

    public SkinImageFile {
        bytes = bytes == null ? new byte[0] : Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public byte[] bytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }
}
