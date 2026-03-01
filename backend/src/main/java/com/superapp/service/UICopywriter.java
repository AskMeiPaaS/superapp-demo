package com.superapp.service;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface UICopywriter {
    @UserMessage("""
        Write a short, punchy 3-5 word button label for a mobile app.
        Context: {{context}}
        Goal: {{goal}}
        Reply ONLY with the label text.
        """)
    String generateLabel(@V("context") String context, @V("goal") String goal);
}