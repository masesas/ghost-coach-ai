package com.playmotech.ghostcoach.ai;

import com.playmotech.ghostcoach.ai.dto.FeedbackReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FeedbackParserTest {

    private FeedbackParser parser;

    @BeforeEach
    void setUp() {
        parser = new FeedbackParser(new ObjectMapper());
    }

    @Test
    @DisplayName("null input returns empty")
    void nullInput() {
        assertThat(parser.parse(null)).isEmpty();
    }

    @Test
    @DisplayName("blank input returns empty")
    void blankInput() {
        assertThat(parser.parse("   ")).isEmpty();
    }

    @Test
    @DisplayName("plain JSON parsed")
    void plainJson() {
        String json = """
                {"overallScore": 7.5, "strengths": ["a","b"], "areasToImprove": [],
                 "priorityFix": "fix it", "drillSuggestion": "do it", "confidenceLevel": "MEDIUM"}
                """;
        Optional<FeedbackReport> result = parser.parse(json);
        assertThat(result).isPresent();
        FeedbackReport r = result.get();
        assertThat(r.overallScore()).isEqualByComparingTo(BigDecimal.valueOf(7.5));
        assertThat(r.strengths()).containsExactly("a", "b");
        assertThat(r.confidenceLevel()).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("JSON wrapped in markdown code fence with language tag")
    void codeFenceWithLanguage() {
        String raw = """
                ```json
                {"overallScore": 9.0, "strengths": [], "areasToImprove": [],
                 "priorityFix": "fix", "drillSuggestion": "drill", "confidenceLevel": "HIGH"}
                ```
                """;
        assertThat(parser.parse(raw)).isPresent();
    }

    @Test
    @DisplayName("JSON wrapped in markdown code fence without language tag")
    void codeFenceNoLanguage() {
        String raw = """
                ```
                {"overallScore": 5.0, "strengths": [], "areasToImprove": [],
                 "priorityFix": "fix", "drillSuggestion": "drill", "confidenceLevel": "LOW"}
                ```
                """;
        assertThat(parser.parse(raw)).isPresent();
    }

    @Test
    @DisplayName("JSON embedded in narrative text → extracted by first balanced brace")
    void jsonEmbeddedInText() {
        String raw = """
                Sure! Here is the JSON you asked for:
                {"overallScore": 6.5, "strengths": ["a"], "areasToImprove": [],
                 "priorityFix": "fix", "drillSuggestion": "drill", "confidenceLevel": "MEDIUM"}
                Hope that helps!
                """;
        Optional<FeedbackReport> result = parser.parse(raw);
        assertThat(result).isPresent();
        assertThat(result.get().overallScore()).isEqualByComparingTo(BigDecimal.valueOf(6.5));
    }

    @Test
    @DisplayName("JSON with closing brace inside string literal → still balanced")
    void braceInsideStringLiteral() {
        String raw = """
                {"overallScore": 5.0, "strengths": ["weird }} text"], "areasToImprove": [],
                 "priorityFix": "} closing", "drillSuggestion": "drill", "confidenceLevel": "LOW"}
                """;
        Optional<FeedbackReport> result = parser.parse(raw);
        assertThat(result).isPresent();
        assertThat(result.get().strengths()).containsExactly("weird }} text");
        assertThat(result.get().priorityFix()).isEqualTo("} closing");
    }

    @Test
    @DisplayName("nested AreaToImprove objects parsed correctly")
    void nestedObjects() {
        String raw = """
                {"overallScore": 8.0,
                 "strengths": ["s1","s2"],
                 "areasToImprove": [
                    {"flaw": "x", "explanation": "y"},
                    {"flaw": "a", "explanation": "b"}
                 ],
                 "priorityFix": "fix", "drillSuggestion": "drill", "confidenceLevel": "HIGH"}
                """;
        Optional<FeedbackReport> result = parser.parse(raw);
        assertThat(result).isPresent();
        assertThat(result.get().areasToImprove()).hasSize(2);
        assertThat(result.get().areasToImprove().get(1).flaw()).isEqualTo("a");
    }

    @Test
    @DisplayName("escaped quote inside string is honored")
    void escapedQuoteInString() {
        String raw = """
                {"overallScore": 7.0,
                 "strengths": ["he said \\"go\\""],
                 "areasToImprove": [],
                 "priorityFix": "fix", "drillSuggestion": "drill", "confidenceLevel": "MEDIUM"}
                """;
        Optional<FeedbackReport> result = parser.parse(raw);
        assertThat(result).isPresent();
        assertThat(result.get().strengths()).containsExactly("he said \"go\"");
    }

    @Test
    @DisplayName("multiple JSON objects → first one extracted")
    void multipleJsonObjectsFirstWins() {
        String raw = """
                {"overallScore": 1.0, "strengths": [], "areasToImprove": [],
                 "priorityFix": "first", "drillSuggestion": "d", "confidenceLevel": "LOW"}
                {"overallScore": 9.0, "strengths": [], "areasToImprove": [],
                 "priorityFix": "second", "drillSuggestion": "d", "confidenceLevel": "HIGH"}
                """;
        Optional<FeedbackReport> result = parser.parse(raw);
        assertThat(result).isPresent();
        assertThat(result.get().priorityFix()).isEqualTo("first");
    }

    @Test
    @DisplayName("unbalanced JSON (no closing brace) → empty + log warn")
    void unbalancedJson() {
        assertThat(parser.parse("{ \"overallScore\": 6.0")).isEmpty();
    }

    @Test
    @DisplayName("invalid JSON (Jackson parser fails) returns empty (logged)")
    void invalidJson() {
        assertThat(parser.parse("{ not valid json }")).isEmpty();
    }

    @Test
    @DisplayName("garbage text without any '{' returns empty")
    void garbage() {
        assertThat(parser.parse("Hello, world! No JSON here.")).isEmpty();
    }

    @Test
    @DisplayName("JSON with missing optional fields parsed with nulls")
    void missingFields() {
        String json = "{\"overallScore\": 6.0}";
        Optional<FeedbackReport> result = parser.parse(json);
        assertThat(result).isPresent();
        assertThat(result.get().overallScore()).isEqualByComparingTo(BigDecimal.valueOf(6.0));
        assertThat(result.get().strengths()).isNull();
    }

    @Test
    @DisplayName("response WITH qualityCheck sufficient=true → field populated")
    void qualityCheckSufficient() {
        String json = """
                {"qualityCheck": {"sufficient": true, "reason": "OK", "detail": ""},
                 "overallScore": 8.0, "strengths": [], "areasToImprove": [],
                 "priorityFix": "fix", "drillSuggestion": "drill", "confidenceLevel": "HIGH"}
                """;
        Optional<FeedbackReport> result = parser.parse(json);
        assertThat(result).isPresent();
        FeedbackReport.QualityCheck qc = result.get().qualityCheck();
        assertThat(qc).isNotNull();
        assertThat(qc.sufficient()).isTrue();
        assertThat(qc.reason()).isEqualTo("OK");
    }

    @Test
    @DisplayName("response WITH qualityCheck sufficient=false → field populated")
    void qualityCheckInsufficient() {
        String json = """
                {"qualityCheck": {"sufficient": false, "reason": "BLURRY", "detail": "Too blurry"},
                 "overallScore": null, "strengths": [], "areasToImprove": [],
                 "priorityFix": "", "drillSuggestion": "", "confidenceLevel": "LOW"}
                """;
        Optional<FeedbackReport> result = parser.parse(json);
        assertThat(result).isPresent();
        FeedbackReport.QualityCheck qc = result.get().qualityCheck();
        assertThat(qc).isNotNull();
        assertThat(qc.sufficient()).isFalse();
        assertThat(qc.reason()).isEqualTo("BLURRY");
        assertThat(qc.detail()).isEqualTo("Too blurry");
    }

    @Test
    @DisplayName("legacy response WITHOUT qualityCheck → field is null, parse OK")
    void qualityCheckLegacyAbsent() {
        String json = """
                {"overallScore": 7.0, "strengths": ["s"], "areasToImprove": [],
                 "priorityFix": "fix", "drillSuggestion": "drill", "confidenceLevel": "MEDIUM"}
                """;
        Optional<FeedbackReport> result = parser.parse(json);
        assertThat(result).isPresent();
        assertThat(result.get().qualityCheck()).isNull();
    }

    @Test
    @DisplayName("partial qualityCheck (only sufficient) → reason/detail null")
    void qualityCheckPartial() {
        String json = """
                {"qualityCheck": {"sufficient": false},
                 "overallScore": null, "strengths": [], "areasToImprove": [],
                 "priorityFix": "", "drillSuggestion": "", "confidenceLevel": "LOW"}
                """;
        Optional<FeedbackReport> result = parser.parse(json);
        assertThat(result).isPresent();
        FeedbackReport.QualityCheck qc = result.get().qualityCheck();
        assertThat(qc).isNotNull();
        assertThat(qc.sufficient()).isFalse();
        assertThat(qc.reason()).isNull();
        assertThat(qc.detail()).isNull();
    }
}
