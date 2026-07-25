package com.xworkz.AIChatBot.controller;

import com.xworkz.AIChatBot.dto.ChatRequest;
import com.xworkz.AIChatBot.dto.ChatResponse;
import com.xworkz.AIChatBot.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = geminiService.getChatResponse(request.getMessage());
        return new ChatResponse(reply);
    }
}