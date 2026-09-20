package org.example;

import java.nio.file.Path;
import java.util.List;

record Decision(String source, String text, double score, List<String> metadata) {
    String context() {
        return "[source: " + source + "]\n" + text;
    }

    static Decision empty(Path path, String text) {
        return new Decision(path.getFileName().toString(), text, 0, List.of());
    }
}
