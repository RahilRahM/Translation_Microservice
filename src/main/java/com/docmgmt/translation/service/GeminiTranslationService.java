package com.docmgmt.translation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public class GeminiTranslationService implements TranslationService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiTranslationService.class);
    private final String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    public GeminiTranslationService(@Value("${app.gemini.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        logger.info("GeminiTranslationService initialized with Gemini 1.5 Flash model");
    }

    @Override
    public String translateText(String text, String sourceLanguage, String targetLanguage) {
        logger.debug("Translating from {} to {}: '{}'", sourceLanguage, targetLanguage, text);
        
        try {
            // Create the request body JSON structure
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contentsArray = objectMapper.createArrayNode();
            ObjectNode content = objectMapper.createObjectNode();
            ArrayNode partsArray = objectMapper.createArrayNode();
            
            ObjectNode textPart = objectMapper.createObjectNode();
            textPart.put("text", createTranslationPrompt(text, sourceLanguage, targetLanguage));
            
            partsArray.add(textPart);
            content.set("parts", partsArray);
            contentsArray.add(content);
            requestBody.set("contents", contentsArray);
            
            // Build the URL with API key
            String url = GEMINI_API_URL + "?key=" + apiKey;
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create HTTP entity with headers and body
            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            
            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            
            // Process the response
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                if (responseJson.has("candidates") && responseJson.get("candidates").size() > 0) {
                    JsonNode candidate = responseJson.get("candidates").get(0);
                    if (candidate.has("content") && candidate.get("content").has("parts")) {
                        String translatedText = candidate.get("content").get("parts").get(0).get("text").asText().trim();
                        logger.debug("Translation result: '{}'", translatedText);
                        return translatedText;
                    }
                }
                throw new RuntimeException("Unable to extract translation from response: " + responseJson);
            } else {
                throw new RuntimeException("Translation API returned status: " + response.getStatusCodeValue());
            }
        } catch (Exception e) {
            logger.error("Translation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Translation failed: " + e.getMessage(), e);
        }
    }

    private String createTranslationPrompt(String text, String sourceLanguage, String targetLanguage) {
        return String.format(
            "Translate the following text from %s to %s. Return ONLY the translated text with no additional comments or formatting:\n\n\"%s\"", 
            sourceLanguage, 
            targetLanguage, 
            text
        );
    }
}
