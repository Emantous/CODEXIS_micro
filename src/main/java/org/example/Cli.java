package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

final class Cli {
    private Cli() {}

    static String readMultilineArgument() throws IOException {
        System.out.println("Vložte argument. Řádek END ukončí vstup:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null && !"END".equals(line.trim())) {
            result.append(line).append('\n');
        }
        return result.toString().trim();
    }

    static void printHeader() {
        System.out.println("""
                ============================================================
                CZECH TENANCY LAW AI — COUNTERARGUMENT ANALYSIS
                ============================================================
                """);
    }

    static void printRetrieval(List<Decision> decisions) {
        System.out.println("Načteno relevantních rozhodnutí: " + decisions.size());
        for (int i = 0; i < decisions.size(); i++) {
            Decision decision = decisions.get(i);
            System.out.printf("  %d. %s (skóre %.2f)%n", i + 1, decision.source(), decision.score());
        }
        System.out.println();
    }

    static void writeMarkdownReport(String answer) throws IOException {
        String markdown = "# Analýza možných protiargumentů\n\n"
                + "> AI-generovaná právně-výzkumná pomůcka; nejde o právní radu.\n\n"
                + answer.trim() + "\n";
        java.nio.file.Files.writeString(Path.of("analysis.md"), markdown, StandardCharsets.UTF_8);
    }
}
