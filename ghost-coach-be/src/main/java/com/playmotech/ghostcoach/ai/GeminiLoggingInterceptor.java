package com.playmotech.ghostcoach.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Logs every outbound Gemini call with status and elapsed latency in ms.
 * Useful for spotting slow upstream responses without pulling in Micrometer.
 */
@Slf4j
public class GeminiLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        long start = System.nanoTime();
        try {
            ClientHttpResponse response = execution.execute(request, body);
            long ms = (System.nanoTime() - start) / 1_000_000;
            log.info("Gemini call status={} latency_ms={} method={} uri={}",
                    response.getStatusCode().value(),
                    ms,
                    request.getMethod(),
                    request.getURI());
            return response;
        } catch (IOException ex) {
            long ms = (System.nanoTime() - start) / 1_000_000;
            log.warn("Gemini call failed after {}ms: {}", ms, ex.getMessage());
            throw ex;
        }
    }
}
