package com.docmgmt.translation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;


/**
 * Primary translation service implementation that uses Google's Gemini API
 * for high-quality translations.
 */
@Service
@Primary
public class GeminiTranslationService implements TranslationServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(GeminiTranslationService.class);
    private String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final String ENV_FILE_PATH = "c:\\Users\\hp\\Desktop\\dms_micros\\trans_service\\.env";

    public GeminiTranslationService(@Value("${app.gemini.api-key:}") String configApiKey) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        
        // Try to load API key from different sources
        this.apiKey = configApiKey;
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            this.apiKey = loadApiKeyFromEnvFile();
        }
        
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            this.apiKey = System.getenv("GEMINI_API_KEY");
        }

        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            logger.warn("No Gemini API key found in any source. Translations will fail.");
        } else {
            logger.info("GeminiTranslationService initialized with API key: {}...", this.apiKey.substring(0, 8));
        }
        
        logger.info("GeminiTranslationService is the PRIMARY translation implementation");
    }
    
    private String loadApiKeyFromEnvFile() {
        logger.info("Attempting to load API key from .env file: {}", ENV_FILE_PATH);
        try {
            File envFile = new File(ENV_FILE_PATH);
            if (!envFile.exists()) {
                logger.warn(".env file not found at path: {}", ENV_FILE_PATH);
                return null;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(envFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("GEMINI_API_KEY=")) {
                    String key = line.substring("GEMINI_API_KEY=".length()).trim();
                    logger.info("Successfully loaded API key from .env file");
                    reader.close();
                    return key;
                }
            }
            reader.close();
            logger.warn("GEMINI_API_KEY not found in .env file");
            return null;
        } catch (Exception e) {
            logger.error("Error reading .env file: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String translateText(String text, String sourceLanguage, String targetLanguage) {
        logger.info("Translating text from {} to {} using Gemini API: '{}'", 
                sourceLanguage, targetLanguage, text);
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("Gemini API key is not configured. Set GEMINI_API_KEY environment variable.");
        }
        
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
                throw new RuntimeException("Translation API returned status: " + response.getStatusCode().value());
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
