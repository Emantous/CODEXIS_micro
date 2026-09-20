package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class KnowledgeBase {
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]{3,}");
    private static final Set<String> STOP_WORDS = Set.of(
            "který", "která", "které", "protože", "pokud", "tento", "tato", "bylo",
            "byla", "byly", "její", "jeho", "nájemce", "pronajímatel", "soud", "věci");

    private final Path directory;

    KnowledgeBase(Path directory) {
        this.directory = directory;
    }

    List<Decision> retrieve(String argument, int limit) throws IOException {
        if (!Files.isDirectory(directory)) return List.of();
        List<String> queries = QueryGenerator.queries(argument);
        List<Decision> decisions = new ArrayList<>();
        List<Path> files;
        try (var paths = Files.walk(directory)) {
            files = paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".txt"))
                    .sorted()
                    .toList();
        }
        Map<String, Integer> documentFrequency = new HashMap<>();
        List<Set<String>> documentTerms = new ArrayList<>();
        for (Path file : files) {
            String text = DocumentReader.read(file);
            Set<String> terms = tokens(text);
            documentTerms.add(terms);
            terms.forEach(term -> documentFrequency.merge(term, 1, Integer::sum));
        }
        for (int i = 0; i < files.size(); i++) {
            String text = DocumentReader.read(files.get(i));
            double score = score(queries, documentTerms.get(i), documentFrequency, files.size());
            if (score > 0) decisions.add(new Decision(
                    files.get(i).getFileName().toString(), text, score, Metadata.extract(text)));
        }
        return decisions.stream()
                .sorted(Comparator.comparingDouble(Decision::score).reversed())
                .limit(Math.max(1, limit))
                .toList();
    }

    private static double score(List<String> queries, Set<String> documentTerms,
                                Map<String, Integer> documentFrequency, int documentCount) {
        double score = 0;
        for (String query : queries) {
            Set<String> queryTerms = tokens(query);
            for (String term : queryTerms) {
                if (documentTerms.contains(term)) {
                    double idf = Math.log((documentCount + 1.0) / (documentFrequency.getOrDefault(term, 0) + 1.0)) + 1;
                    score += idf;
                }
            }
        }
        return score / Math.max(1, queries.size());
    }

    static Set<String> tokens(String text) {
        Set<String> result = new HashSet<>();
        var matcher = WORD.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String token = matcher.group();
            if (!STOP_WORDS.contains(token)) result.add(token);
        }
        return result;
    }
}
