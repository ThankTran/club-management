package com.example.demo.ai.controller;

import com.example.demo.ai.dto.request.HelpAiRequest;
import com.example.demo.ai.dto.response.HelpAiResponse;
import com.example.demo.user.dto.response.UserResponse;
import com.example.demo.ai.service.interfaces.HelpAiService;
import com.example.demo.shared.security.AccessControlInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/help")
@RequiredArgsConstructor
public class HelpAiController {
    private final HelpAiService helpAiService;

    @PostMapping
    public HelpAiResponse ask(
            @RequestBody HelpAiRequest request,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_USER_ATTRIBUTE, required = false)
            UserResponse currentUser) {
        return helpAiService.ask(request, currentUser);
    }
}
