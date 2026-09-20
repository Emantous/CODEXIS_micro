package org.example;

import java.util.ArrayList;
import java.util.List;

final class QueryGenerator {
    private QueryGenerator() {}

    static List<String> queries(String argument) {
        List<String> queries = new ArrayList<>();
        queries.add(argument);
        String lower = argument.toLowerCase();
        if (containsAny(lower, "nájemné", "plat", "prodlení", "dluh", "uhra")) {
            queries.add("opožděné platby nájemného výpověď nájem bytu");
            queries.add("opakované prodlení nájemce závažné porušení povinnosti");
            queries.add("uhrazení dluhu nájemcem po prodlení výpověď");
        }
        if (containsAny(lower, "výpověď", "skončení", "neplat")) {
            queries.add("platnost výpovědi z nájmu bytu podmínky");
        }
        if (containsAny(lower, "upozorn", "výzv", "náprav", "lhůt")) {
            queries.add("výzva upozornění nájemce náprava porušení nájemní smlouvy");
        }
        return queries;
    }

    private static boolean containsAny(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
}
