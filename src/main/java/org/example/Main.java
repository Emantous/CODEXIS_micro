package org.example;

import java.util.List;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        try {
            AppConfig config = AppConfig.fromEnvironment();

            String argument = Cli.readMultilineArgument();
            if (argument.isBlank()) {
                System.err.println("ERROR: Zadejte neprázdný argument.");
                System.exit(2);
            }

            KnowledgeBase knowledgeBase = new KnowledgeBase(config.decisionsDirectory());
            List<Decision> decisions = knowledgeBase.retrieve(argument, config.maxSources());
            Cli.printHeader();
            Cli.printRetrieval(decisions);

            if (decisions.isEmpty()) {
                Cli.writeMarkdownReport("""
                        ## Shrnutí pozice

                        Nebyla nalezena dostatečně relevantní česká judikatura v dostupné znalostní bázi.
                        Systém proto nemůže vytvořit zdrojově podložený protiargument.
                        """);
                System.out.println("Markdown report vytvořen: analysis.md");
                return;
            }

            if (config.apiKey() == null || config.apiKey().isBlank()) {
                System.err.println("ERROR: Chybí OPENAI_API_KEY. Nastavte jej v prostředí nebo v souboru .env.");
                System.exit(2);
            }

            String answer = new LegalReasoner(config).analyze(argument, decisions);
            String report = CitationValidator.validate(answer, decisions);
            Cli.writeMarkdownReport(report);
            System.out.println("Markdown report vytvořen: analysis.md");
        } catch (Exception exception) {
            System.err.println("ERROR: " + exception.getMessage());
            if (Boolean.getBoolean("debug")) {
                exception.printStackTrace(System.err);
            }
            System.exit(1);
        }
    }

}
