package com.playmotech.ghostcoach.ai;

import com.playmotech.ghostcoach.ai.dto.GeminiRequest;
import com.playmotech.ghostcoach.common.exception.ApiException;
import com.playmotech.ghostcoach.config.AppConfigProp;
import com.playmotech.ghostcoach.prompt.ModelConfig;
import com.playmotech.ghostcoach.prompt.PromptResponseFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiClientTest {

    private static final String API_KEY = "test-key-xyz-123";
    private static final String BASE_URL = "https://example.test/v1beta";
    private static final String MODEL = "gemini-1.5-flash";
    private static final String EXPECTED_URL = BASE_URL + "/models/" + MODEL + ":generateContent";

    private GeminiClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        AppConfigProp props = new AppConfigProp();
        AppConfigProp.Gemini gemini = new AppConfigProp.Gemini();
        gemini.setApiKey(API_KEY);
        gemini.setBaseUrl(BASE_URL);
        gemini.setModel(MODEL);
        props.setGemini(gemini);

        client = new GeminiClient(restClient, props);
        client.validateConfig();
    }

    @Test
    @DisplayName("URL does not contain api key query parameter")
    void urlHasNoApiKeyQueryParam() {
        server.expect(requestTo(EXPECTED_URL))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withSuccess(successJson(), MediaType.APPLICATION_JSON));

        client.generate(GeminiRequest.textOnly("hello"), null, null);

        server.verify();
        assertThat(EXPECTED_URL).doesNotContain("key=");
    }

    @Test
    @DisplayName("API key is sent via x-goog-api-key header")
    void apiKeyInHeader() {
        server.expect(requestTo(EXPECTED_URL))
                .andExpect(header("x-goog-api-key", API_KEY))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(successJson(), MediaType.APPLICATION_JSON));

        String result = client.generate(GeminiRequest.textOnly("ping"), null, null);

        assertThat(result).isEqualTo("hello from AI");
        server.verify();
    }

    @Test
    @DisplayName("empty candidates response → AI_NO_RESPONSE")
    void emptyCandidates() {
        server.expect(requestTo(EXPECTED_URL))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.generate(GeminiRequest.textOnly("ping"), null, null))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("empty");
    }

    @Test
    @DisplayName("server 5xx → AI_ERROR")
    void serverError() {
        server.expect(requestTo(EXPECTED_URL))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.generate(GeminiRequest.textOnly("ping"), null, null))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Failed to get response");
    }

    @Test
    @DisplayName("override model is used in URL, replacing YAML default")
    void overrideModelGoesToUrl() {
        String overrideModel = "gemini-2.0-flash";
        String overrideUrl = BASE_URL + "/models/" + overrideModel + ":generateContent";
        server.expect(requestTo(overrideUrl))
                .andRespond(withSuccess(successJson(), MediaType.APPLICATION_JSON));

        client.generate(
                GeminiRequest.textOnly("ping"),
                new ModelConfig(overrideModel, null, null),
                PromptResponseFormat.TEXT
        );
        server.verify();
    }

    @Test
    @DisplayName("null override falls back to YAML model and omits generationConfig")
    void nullOverrideFallsBackToYaml() {
        server.expect(requestTo(EXPECTED_URL))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("generationConfig"))))
                .andRespond(withSuccess(successJson(), MediaType.APPLICATION_JSON));

        client.generate(GeminiRequest.textOnly("ping"), null, null);
        server.verify();
    }

    @Test
    @DisplayName("override temperature and maxOutputTokens are serialized in generationConfig")
    void temperatureAndMaxTokensSerialized() {
        server.expect(requestTo(EXPECTED_URL))
                .andExpect(jsonPath("$.generationConfig.temperature").value(0.4))
                .andExpect(jsonPath("$.generationConfig.maxOutputTokens").value(1024))
                .andRespond(withSuccess(successJson(), MediaType.APPLICATION_JSON));

        client.generate(
                GeminiRequest.textOnly("ping"),
                new ModelConfig(null, 0.4, 1024),
                PromptResponseFormat.TEXT
        );
        server.verify();
    }

    @Test
    @DisplayName("JSON format sets responseMimeType=application/json")
    void jsonFormatSetsResponseMimeType() {
        server.expect(requestTo(EXPECTED_URL))
                .andExpect(jsonPath("$.generationConfig.responseMimeType").value("application/json"))
                .andRespond(withSuccess(successJson(), MediaType.APPLICATION_JSON));

        client.generate(
                GeminiRequest.textOnly("ping"),
                new ModelConfig(null, null, null),
                PromptResponseFormat.JSON
        );
        server.verify();
    }

    @Test
    @DisplayName("TEXT format does NOT set responseMimeType")
    void textFormatOmitsResponseMimeType() {
        server.expect(requestTo(EXPECTED_URL))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("responseMimeType"))))
                .andRespond(withSuccess(successJson(), MediaType.APPLICATION_JSON));

        client.generate(
                GeminiRequest.textOnly("ping"),
                new ModelConfig(null, 0.5, null),
                PromptResponseFormat.TEXT
        );
        server.verify();
    }

    @Test
    @DisplayName("blank override model falls back to YAML")
    void blankOverrideModelFallsBack() {
        server.expect(requestTo(EXPECTED_URL))
                .andRespond(withSuccess(successJson(), MediaType.APPLICATION_JSON));

        client.generate(
                GeminiRequest.textOnly("ping"),
                new ModelConfig("  ", null, null),
                PromptResponseFormat.TEXT
        );
        server.verify();
    }

    private static String successJson() {
        return """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {"text": "hello from AI"}
                        ]
                      }
                    }
                  ]
                }
                """;
    }
}
