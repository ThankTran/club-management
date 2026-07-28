package com.example.demo.ai.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.demo.ai.service.impl.GroqHttpChatClient;
import org.junit.jupiter.api.Test;

class GroqHttpChatClientTest {
    @Test
    void parsesFirstChoiceMessageContent() {
        String json = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "Xin chao tu Groq"
                      }
                    }
                  ]
                }
                """;

        assertEquals("Xin chao tu Groq", GroqHttpChatClient.parseAnswer(json));
    }
}
