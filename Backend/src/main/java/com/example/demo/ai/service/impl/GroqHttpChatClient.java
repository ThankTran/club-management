package com.example.demo.ai.service.impl;

import com.example.demo.ai.service.interfaces.GroqChatClient;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GroqHttpChatClient implements GroqChatClient {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);
    private static final Pattern CONTENT_PATTERN = Pattern.compile(
            "\"choices\"\\s*:\\s*\\[.*?\"message\"\\s*:\\s*\\{.*?\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
            Pattern.DOTALL);

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public GroqHttpChatClient(
            @Value("${groq.api-key:}") String apiKey,
            @Value("${groq.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${groq.model:llama-3.3-70b-versatile}") String model) {
        this(HttpClient.newHttpClient(), apiKey, baseUrl, model);
    }

    GroqHttpChatClient(HttpClient httpClient, String apiKey, String baseUrl, String model) {
        this.httpClient = httpClient;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "https://api.groq.com/openai/v1"
                : baseUrl.trim();
        this.model = model == null || model.isBlank() ? "llama-3.3-70b-versatile" : model.trim();
    }

    @Override
    public String complete(String systemPrompt, String userMessage) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Missing GROQ_API_KEY");
        }

        try {
            String body = buildRequestBody(systemPrompt, userMessage);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint()))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Groq request failed with status " + response.statusCode());
            }
            return parseAnswer(response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Groq API call interrupted", ex);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot call Groq API", ex);
        }
    }

    private String endpoint() {
        return baseUrl.replaceAll("/+$", "") + "/chat/completions";
    }

    public static String parseAnswer(String responseBody) {
        Matcher matcher = CONTENT_PATTERN.matcher(responseBody == null ? "" : responseBody);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Groq response does not contain message content");
        }
        String answer = unescapeJson(matcher.group(1)).trim();
        if (answer.isBlank()) {
            throw new IllegalArgumentException("Groq response does not contain message content");
        }
        return answer;
    }

    private String buildRequestBody(String systemPrompt, String userMessage) {
        return """
                {"model":"%s","messages":[{"role":"system","content":"%s"},{"role":"user","content":"%s"}],"temperature":0.2,"max_completion_tokens":700}
                """.formatted(escapeJson(model), escapeJson(systemPrompt), escapeJson(userMessage));
    }

    private static String escapeJson(String value) {
        return (value == null ? "" : value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (!escaped) {
                if (current == '\\') {
                    escaped = true;
                } else {
                    result.append(current);
                }
                continue;
            }

            switch (current) {
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case '"' -> result.append('"');
                case '\\' -> result.append('\\');
                default -> result.append(current);
            }
            escaped = false;
        }
        if (escaped) {
            result.append('\\');
        }
        return result.toString();
    }
}
