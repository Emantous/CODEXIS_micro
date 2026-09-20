package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

record AppConfig(
        String apiKey,
        String model,
        Path decisionsDirectory,
        int maxSources,
        boolean debug) {

    static AppConfig fromEnvironment() {
        Map<String, String> values = new HashMap<>();
        Path env = Path.of(".env");
        if (Files.isRegularFile(env)) {
            try {
                for (String line : Files.readAllLines(env)) {
                    String trimmed = line.trim();
                    if (!trimmed.isBlank() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                        int separator = trimmed.indexOf('=');
                        values.putIfAbsent(trimmed.substring(0, separator).trim(),
                                trimmed.substring(separator + 1).trim());
                    }
                }
            } catch (Exception exception) {
                throw new IllegalStateException("Nelze načíst .env: " + exception.getMessage(), exception);
            }
        }
        return new AppConfig(
                first(values, "OPENAI_API_KEY"),
                firstOr(values, "OPENAI_MODEL", "gpt-4.1-mini"),
                Path.of(firstOr(values, "DECISIONS_DIR", "data/decisions")),
                Integer.parseInt(firstOr(values, "MAX_SOURCES", "8")),
                Boolean.parseBoolean(firstOr(values, "DEBUG", "false")));
    }

    private static String first(Map<String, String> values, String key) {
        String environment = System.getenv(key);
        return environment != null && !environment.isBlank() ? environment : values.get(key);
    }

    private static String firstOr(Map<String, String> values, String key, String fallback) {
        String value = first(values, key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
