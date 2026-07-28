package com.example.demo.ai.service.interfaces;

import com.example.demo.ai.dto.request.HelpAiRequest;
import com.example.demo.ai.dto.response.HelpAiResponse;
import com.example.demo.user.dto.response.UserResponse;

public interface HelpAiService {
    HelpAiResponse ask(HelpAiRequest request, UserResponse currentUser);
}
