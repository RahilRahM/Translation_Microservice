package com.docmgmt.translation.kafka;

import com.docmgmt.translation.model.TranslationRequest;
import com.docmgmt.translation.model.TranslationResponse;
import com.docmgmt.translation.model.TranslationResponse.Status;
import com.docmgmt.translation.service.TranslationServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    
    private final TranslationServiceInterface translationService;
    private final KafkaProducer kafkaProducer;
    
    @Autowired
    public KafkaConsumer(TranslationServiceInterface translationService, KafkaProducer kafkaProducer) {
        this.translationService = translationService;
        this.kafkaProducer = kafkaProducer;
        logger.info("Kafka consumer initialized");
    }

    @KafkaListener(
        topics = "${app.topics.translation-request}", 
        containerFactory = "kafkaListenerContainerFactory",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeSimpleMessage(String message) {
        logger.info("Received translation request message: {}", message);
        
        try {
            // Parse the message format "docId:title"
            String[] parts = message.split(":", 2);
            
            if (parts.length != 2) {
                logger.error("Invalid message format. Expected 'docId:title', got: {}", message);
                return;
            }
            
            // Create a TranslationRequest from the simple message
            TranslationRequest request = new TranslationRequest();
            request.setDocumentId(parts[0]); // UUID comes as string
            request.setTitle(parts[1]);
            request.setSourceLanguage("en"); // Default source language
            request.setTargetLanguage("es"); // Default target language
            
            processTranslationRequest(request);
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
        }
    }
    
    private void processTranslationRequest(TranslationRequest request) {
        try {
            // Translate the title using the translation service
            String translatedTitle = translationService.translateText(
                request.getTitle(),
                request.getSourceLanguage(),
                request.getTargetLanguage()
            );
            
            // Create response with the translated title
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
            
            // Send response back to document service
            kafkaProducer.sendTranslationResponse(response);
            logger.info("Sent translation response for document: {}", request.getDocumentId());
        } catch (Exception e) {
            // Create error response
            TranslationResponse errorResponse = new TranslationResponse(
                request.getDocumentId(),
                request.getTitle(),
                null, // No translated title
                request.getSourceLanguage(),
                request.getTargetLanguage(),
                Status.FAILED,
                e.getMessage(),
                LocalDateTime.now().toString()
            );
            
            // Send error response back to document service
            kafkaProducer.sendTranslationResponse(errorResponse);
            logger.error("Error processing translation: {}", e.getMessage(), e);
        }
    }
}
