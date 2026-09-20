package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

final class LegalReasoner {
    private static final String INSTRUCTIONS = """
            Jsi asistent pro právní výzkum zaměřený na české nájemní právo.
            Nejsi advokát a neposkytuješ definitivní právní radu.
            Analyzuj argument uživatele výhradně ve vztahu k dodaným rozhodnutím.
            Nevymýšlej soudy, spisové značky, data, citace ani závěry.
            Každý důležitý protiargument musí uvést jednu nebo více přesných značek
            zdrojů ve formátu [source: název_souboru.txt]. Používej pouze zdroje z podkladů.
            Rozliš přímé tvrzení zdroje od vlastní inference. Pokud jsou podklady slabé,
            řekni to, ale nevytvářej samostatnou sekci s omezeními ani závěrem.
            Odpověz česky v této struktuře:
            ## Shrnutí pozice
            ## Identifikované právní otázky
            ## Možné protiargumenty
            ## Relevantní rozhodnutí
            """;

    private final AppConfig config;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    LegalReasoner(AppConfig config) {
        this.config = config;
    }

    String analyze(String argument, List<Decision> decisions) throws Exception {
        StringBuilder context = new StringBuilder();
        for (Decision decision : decisions) {
            context.append("\n---\n").append(decision.context());
        }
        String prompt = "ARGUMENT UŽIVATELE:\n" + argument + "\n\nDODANÁ JUDIKATURA:" + context;
        String body = mapper.createObjectNode()
                .put("model", config.model())
                .put("instructions", INSTRUCTIONS)
                .put("input", prompt)
                .toString();
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/responses"))
                .timeout(Duration.ofMinutes(2))
                .header("Authorization", "Bearer " + config.apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("OpenAI API chyba HTTP " + response.statusCode() + ": "
                    + compactError(response.body()));
        }
        JsonNode root = mapper.readTree(response.body());
        StringBuilder output = new StringBuilder();
        collectOutputText(root, output);
        if (output.isEmpty()) throw new IllegalStateException("OpenAI API nevrátil textovou odpověď.");
        return output.toString().trim();
    }

    private static void collectOutputText(JsonNode node, StringBuilder output) {
        if (node.isObject()) {
            if ("output_text".equals(node.path("type").asText()) && node.has("text")) {
                output.append(node.path("text").asText()).append('\n');
            }
            node.fields().forEachRemaining(entry -> collectOutputText(entry.getValue(), output));
        } else if (node.isArray()) {
            node.forEach(child -> collectOutputText(child, output));
        }
    }

    private String compactError(String body) {
        try {
            return mapper.readTree(body).path("error").path("message").asText(body);
        } catch (Exception ignored) {
            return body.length() > 300 ? body.substring(0, 300) : body;
        }
    }
}
