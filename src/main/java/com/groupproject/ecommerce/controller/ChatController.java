package com.groupproject.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpSession;
import com.groupproject.ecommerce.service.BookService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String openaiUrl;

    @Autowired
    private BookService bookService;

    private static final String CHAT_HISTORY_KEY = "chatHistory";

    private String buildSystemPrompt() {
        return """
            You are a helpful book store assistant. Help users find books based on their preferences.
            Keep responses concise and friendly. Recommend 1-3 books based on user needs.
            If they want something specific, guide them to the right book.
            Include the book price when recommending.
            
            """ + bookService.getBooksForPrompt();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> getChatHistory(HttpSession session) {
        List<Map<String, String>> history = (List<Map<String, String>>) session.getAttribute(CHAT_HISTORY_KEY);
        if (history == null) {
            history = new ArrayList<>();
            session.setAttribute(CHAT_HISTORY_KEY, history);
        }
        return history;
    }

    @GetMapping("/chat/history")
    public ResponseEntity<List<Map<String, String>>> getHistory(HttpSession session) {
        return ResponseEntity.ok(getChatHistory(session));
    }

    @PostMapping("/chat/clear")
    public ResponseEntity<Map<String, String>> clearHistory(HttpSession session) {
        session.removeAttribute(CHAT_HISTORY_KEY);
        return ResponseEntity.ok(Map.of("status", "cleared"));
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request, HttpSession session) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }

        try {
            List<Map<String, String>> history = getChatHistory(session);
            history.add(Map.of("role", "user", "content", userMessage));
            
            String response = callOpenAI(history);
            history.add(Map.of("role", "bot", "content", response));
            
            if (history.size() > 20) {
                history.subList(0, history.size() - 20).clear();
            }
            
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get response: " + e.getMessage()));
        }
    }

    private String callOpenAI(List<Map<String, String>> history) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt()));
        
        for (Map<String, String> msg : history) {
            String role = msg.get("role").equals("user") ? "user" : "assistant";
            messages.add(Map.of("role", role, "content", msg.get("content")));
        }

        Map<String, Object> requestBody = Map.of(
            "model", "gpt-4o-mini",
            "messages", messages,
            "max_tokens", 500
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(openaiUrl, entity, Map.class);

        if (response != null && response.containsKey("choices")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }

        return "I'm sorry, I couldn't process your request. Please try again.";
    }
}
