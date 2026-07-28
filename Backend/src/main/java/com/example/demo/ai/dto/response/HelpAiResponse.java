package com.example.demo.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpAiResponse {
    public String intent;
    public String source;
    public String answer;
    public List<String> suggestions;

}
