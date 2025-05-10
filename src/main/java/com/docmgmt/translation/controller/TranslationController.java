package com.docmgmt.translation.controller;

import com.docmgmt.translation.model.TranslationRequest;
import com.docmgmt.translation.model.TranslationResponse;
import com.docmgmt.translation.model.TranslationResponse.Status;
import com.docmgmt.translation.service.TranslationServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    private static final Logger logger = LoggerFactory.getLogger(TranslationController.class);
    private final TranslationServiceInterface translationService;
    private final RestTemplate restTemplate;
    private final String apiKey;

    @Autowired
    public TranslationController(TranslationServiceInterface translationService, 
                                @Value("${app.gemini.api-key}") String apiKey) {
        this.translationService = translationService;
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        logger.info("TranslationController initialized");
    }

    @PostMapping
    public ResponseEntity<TranslationResponse> translateTitle(@RequestBody TranslationRequest request) {
        logger.info("Received translation request for document: {}", request.getDocumentId());
        
        try {
            // Call the translation service
            String translatedTitle = translationService.translateText(
                request.getTitle(),
                request.getSourceLanguage(),
                request.getTargetLanguage()
            );
            
            // Create response
            TranslationResponse response = new TranslationResponse(
                request.getDocumentId(),
                request.getTitle(),
                translatedTitle,
                request.getSourceLanguage(),
                request.getTargetLanguage(),
                Status.COMPLETED,
                null, // No error
                LocalDateTime.now().toString()
            );
            
            logger.info("Successfully translated title for document: {}", request.getDocumentId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Translation failed: {}", e.getMessage(), e);
            
            // Create error response
            TranslationResponse errorResponse = new TranslationResponse(
                request.getDocumentId(),
                request.getTitle(),
                null, // No translation
                request.getSourceLanguage(),
                request.getTargetLanguage(),
                Status.FAILED,
                e.getMessage(),
                LocalDateTime.now().toString()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/models")
    public ResponseEntity<String> listAvailableModels() {
        try {
            String listModelsUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            ResponseEntity<String> response = restTemplate.getForEntity(listModelsUrl, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error fetching models: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error fetching models: " + e.getMessage());
        }
    }
}
