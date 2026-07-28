package com.example.demo.ai.service.interfaces;

public interface GroqChatClient {
    String complete(String systemPrompt, String userMessage);
}
