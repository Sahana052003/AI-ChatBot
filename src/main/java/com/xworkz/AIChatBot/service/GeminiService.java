package com.xworkz.AIChatBot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = createRestTemplate();

    private final List<Map<String, Object>> conversationHistory = new ArrayList<Map<String, Object>>();

    private boolean systemPromptSent = false;

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }

    public String getChatResponse(String userMessage) {

        // Inject persona instruction only once, at the start of the conversation
        if (!systemPromptSent) {
            Map<String, Object> systemTurn = new HashMap<String, Object>();
            systemTurn.put("role", "user");

            List<Map<String, String>> systemParts = new ArrayList<Map<String, String>>();
            Map<String, String> systemText = new HashMap<String, String>();
            systemText.put("text", "From now on, your name is Sora. Introduce yourself as Sora if asked, and keep responses friendly and concise.");
            systemParts.add(systemText);
            systemTurn.put("parts", systemParts);
            conversationHistory.add(systemTurn);

            Map<String, Object> ackTurn = new HashMap<String, Object>();
            ackTurn.put("role", "model");
            List<Map<String, String>> ackParts = new ArrayList<Map<String, String>>();
            Map<String, String> ackText = new HashMap<String, String>();
            ackText.put("text", "Understood! I'm Sora, ready to help.");
            ackParts.add(ackText);
            ackTurn.put("parts", ackParts);
            conversationHistory.add(ackTurn);

            systemPromptSent = true;
        }

        Map<String, Object> userTurn = new HashMap<String, Object>();
        userTurn.put("role", "user");

        List<Map<String, String>> userParts = new ArrayList<Map<String, String>>();
        Map<String, String> userText = new HashMap<String, String>();
        userText.put("text", userMessage);
        userParts.add(userText);
        userTurn.put("parts", userParts);

        conversationHistory.add(userTurn);

        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("contents", conversationHistory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(requestBody, headers);

        String urlWithKey = apiUrl + "?key=" + apiKey;

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, entity, Map.class);
            String reply = extractReply(response.getBody());

            Map<String, Object> modelTurn = new HashMap<String, Object>();
            modelTurn.put("role", "model");

            List<Map<String, String>> modelParts = new ArrayList<Map<String, String>>();
            Map<String, String> modelText = new HashMap<String, String>();
            modelText.put("text", reply);
            modelParts.add(modelText);
            modelTurn.put("parts", modelParts);

            conversationHistory.add(modelTurn);

            return reply;

        } catch (Exception e) {
            return "Sorry, something went wrong: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractReply(Map responseBody) {
        List candidates = (List) responseBody.get("candidates");
        Map firstCandidate = (Map) candidates.get(0);
        Map content = (Map) firstCandidate.get("content");
        List parts = (List) content.get("parts");
        Map firstPart = (Map) parts.get(0);
        return (String) firstPart.get("text");
    }
}