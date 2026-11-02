package com.shopit.service;

import java.util.Arrays;
import java.util.stream.Collectors;

/** RFC-4180-style CSV quoting plus spreadsheet formula-prefix neutralization. */
public final class CsvExportSupport {
    private CsvExportSupport() {
    }

    public static String row(Object... values) {
        return Arrays.stream(values)
                .map(CsvExportSupport::cell)
                .collect(Collectors.joining(","))
                + "\r\n";
    }

    public static String cell(Object value) {
        String text = value == null ? "" : value.toString();
        String firstNonWhitespace = text.stripLeading();
        if (!firstNonWhitespace.isEmpty()) {
            char first = firstNonWhitespace.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@') {
                text = "'" + text;
            }
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
