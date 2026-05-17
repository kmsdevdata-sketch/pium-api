package com.pium.adapter.inbound.response;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class ApiDateTimeFormatter {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter ISO_INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private ApiDateTimeFormatter() {
    }

    public static String format(LocalDateTime value) {
        return ISO_INSTANT_FORMATTER.format(value.atZone(ASIA_SEOUL).toInstant());
    }
}
