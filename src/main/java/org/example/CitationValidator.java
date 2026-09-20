package org.example;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CitationValidator {
    private static final Pattern CITATION = Pattern.compile("\\[source:\\s*([^\\]]+)]");

    private CitationValidator() {}

    static String validate(String answer, List<Decision> decisions) {
        Set<String> valid = new HashSet<>();
        decisions.forEach(decision -> valid.add(decision.source()));
        var matcher = CITATION.matcher(answer);
        StringBuffer sanitized = new StringBuffer();
        while (matcher.find()) {
            if (!valid.contains(matcher.group(1).trim())) {
                matcher.appendReplacement(sanitized, "");
            } else {
                matcher.appendReplacement(sanitized, Matcher.quoteReplacement(matcher.group()));
            }
        }
        matcher.appendTail(sanitized);
        return removeTrailingSections(sanitized.toString());
    }

    private static String removeTrailingSections(String answer) {
        return answer.replaceAll(
                "(?is)\\n+##\\s*(?:Omezení analýzy|Závěr|VAROVÁNÍ)\\b.*$", "").trim();
    }
}
