package com.example.demo.ai.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.ai.dto.request.HelpAiRequest;
import com.example.demo.user.dto.response.UserResponse;
import com.example.demo.ai.service.impl.HelpAiServiceImpl;
import com.example.demo.ai.service.interfaces.GroqChatClient;
import com.example.demo.ai.service.interfaces.HelpAiService;
import org.junit.jupiter.api.Test;

class HelpAiServiceTest {
    @Test
    void memberPromptDoesNotIncludeManagerPermissions() {
        CapturingGroqChatClient groq = new CapturingGroqChatClient("Member answer");
        HelpAiService service = new HelpAiServiceImpl(groq);

        service.ask(request("Quan ly thanh vien nhu the nao?"), memberUser());

        assertTrue(groq.systemPrompt.contains("Trợ lý AI"));
        assertTrue(groq.systemPrompt.contains("Trang hiện tại: /help"));
        assertTrue(groq.systemPrompt.contains("profile"));
        assertTrue(groq.systemPrompt.contains("eventuser"));
        assertFalse(groq.systemPrompt.contains("memberadmin"));
        assertFalse(groq.systemPrompt.contains("settings"));
    }

    @Test
    void managerPromptIncludesManagerPermissions() {
        CapturingGroqChatClient groq = new CapturingGroqChatClient("Manager answer");
        HelpAiService service = new HelpAiServiceImpl(groq);

        service.ask(request("Quan ly thanh vien nhu the nao?"), managerUser());

        assertTrue(groq.systemPrompt.contains("memberadmin"));
        assertTrue(groq.systemPrompt.contains("settings"));
    }

    @Test
    void missingGroqKeyFallsBackWithoutCrashing() {
        HelpAiService service = new HelpAiServiceImpl((systemPrompt, userMessage) -> {
            throw new IllegalStateException("Missing GROQ_API_KEY");
        });

        var response = service.ask(request("dang ky su kien"), memberUser());

        assertEquals("FALLBACK", response.getSource());
        assertTrue(response.getAnswer().length() > 0);
        assertFalse(response.getAnswer().contains("Missing GROQ_API_KEY"));
        assertTrue(response.getAnswer().contains("Bạn vào mục Sự kiện"));
    }

    @Test
    void groqAnswerIsMarkedAsRealAi() {
        HelpAiService service = new HelpAiServiceImpl((systemPrompt, userMessage) -> "Trả lời từ Groq");

        var response = service.ask(request("dang ky su kien"), memberUser());

        assertEquals("REAL_AI", response.getSource());
        assertEquals("Trả lời từ Groq", response.getAnswer());
    }

    private HelpAiRequest request(String message) {
        HelpAiRequest request = new HelpAiRequest();
        request.setMessage(message);
        request.setPage("/help");
        return request;
    }

    private UserResponse memberUser() {
        return UserResponse.builder()
                .userId(10L)
                .memberId(20L)
                .roleName("Thanh vien")
                .rolePriority(10)
                .build();
    }

    private UserResponse managerUser() {
        return UserResponse.builder()
                .userId(1L)
                .memberId(2L)
                .roleName("Chu nhiem")
                .rolePriority(1)
                .build();
    }

    private static class CapturingGroqChatClient implements GroqChatClient {
        private final String answer;
        private String systemPrompt;

        CapturingGroqChatClient(String answer) {
            this.answer = answer;
        }

        @Override
        public String complete(String systemPrompt, String userMessage) {
            this.systemPrompt = systemPrompt;
            return answer;
        }
    }
}
