package com.daniil.booksonline.dto;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;

@Builder
public record ApiErrorResponseDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}

