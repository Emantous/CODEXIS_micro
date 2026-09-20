# Czech Tenancy Law AI

Terminálový proof of concept pro hledání možných protiargumentů v českých
sporech o nájem bytu. Aplikace nejprve vyhledá relevantní dokumenty v lokální
sbírce rozhodnutí, poté předá pouze tyto podklady modelu OpenAI Responses API.
Výstup obsahuje ověřované značky zdrojů ve tvaru `[source: soubor.txt]`.

## Architektura

```text
argument v terminálu
  -> analýza dotazu a lokální TF-IDF-like retrieval
  -> 5–15 nejrelevantnějších textových rozhodnutí
  -> jeden reasoning call přes Responses API
  -> kontrola zdrojových značek
  -> čitelný Markdown report `analysis.md`
```

## Instalace a konfigurace

Vyžaduje Java 25+ a Maven. Nastavte konfiguraci:

```bash
cp .env.example .env
# doplňte OPENAI_API_KEY
```

Rozhodnutí v `data/decisions/` jsou načtena automaticky; další `.txt` soubory
můžete přidat do stejné složky. Čte se nejprve
striktní UTF-8 a při neúspěchu Windows-1250, což odpovídá dodanému korpusu.
Název souboru je vždy dostupný jako zdrojový identifikátor; metadata jako soud,
spisová značka a datum se použijí jen tehdy, pokud jsou v textu výslovně uvedena.

## Příprava a spuštění

```bash
mvn package
mvn exec:java -Dexec.mainClass=org.example.Main
```

V interaktivním režimu vložte více řádků a ukončete je řádkem `END`.
Po dokončení se report uloží jako `analysis.md` v kořeni projektu.

Indexer je lokální a deterministický, takže se při každém spuštění nevytváří
nový externí vector store. Pokud je corpus později připojen k hosted vector
store, lze lokální retrieval nahradit bez změny CLI a validační vrstvy.

## Omezení

Corpus je načten z `data/decisions/`. Aplikace
neposkytuje právní radu, negarantuje úplnost ani správnost interpretace a
nemůže rozhodnout spor. Bez relevantních dokumentů nevytváří protiargumenty.
Model smí citovat pouze načtené zdroje; neověřené značky jsou na výstupu
označeny varováním.
