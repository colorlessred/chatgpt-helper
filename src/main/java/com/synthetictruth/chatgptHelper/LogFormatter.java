package com.synthetictruth.chatgptHelper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        Instant i = record.getInstant();
        String ts = i.atZone(ZoneId.systemDefault()).format(formatter);
        return String.format("%s: %s", ts, record.getMessage());
    }
}
