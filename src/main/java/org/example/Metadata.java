package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class Metadata {
    private static final List<Pattern> PATTERNS = List.of(
            Pattern.compile("(?im)^\\s*(?:soud|court)\\s*:\\s*(.+)$"),
            Pattern.compile("(?im)^\\s*(?:sp\\.\\s*zn\\.?|spisová značka)\\s*:?\\s*(.+)$"),
            Pattern.compile("(?im)^\\s*(?:datum|date)\\s*:\\s*(.+)$"));

    private Metadata() {}

    static List<String> extract(String text) {
        List<String> values = new ArrayList<>();
        for (Pattern pattern : PATTERNS) {
            var matcher = pattern.matcher(text);
            if (matcher.find()) values.add(matcher.group(1).trim());
        }
        return values;
    }
}
