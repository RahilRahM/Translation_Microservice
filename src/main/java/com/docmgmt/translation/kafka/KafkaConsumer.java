package com.docmgmt.translation.kafka;

import com.docmgmt.translation.model.TranslationRequest;
import com.docmgmt.translation.model.TranslationResponse;
import com.docmgmt.translation.model.TranslationResponse.Status;
import com.docmgmt.translation.service.TranslationService;
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
    
    private final TranslationService translationService;
    private final KafkaProducer kafkaProducer;
    
    @Autowired
    public KafkaConsumer(TranslationService translationService, KafkaProducer kafkaProducer) {
        this.translationService = translationService;
        this.kafkaProducer = kafkaProducer;
        logger.info("Kafka consumer initialized");
    }

    @KafkaListener(topics = "${app.topics.translation-request}")
    public void consumeTranslationRequest(TranslationRequest request) {
        logger.info("Received translation request for document: {}", request.getDocumentId());
        
        try {
            String translatedTitle = translationService.translateText(
                request.getTitle(),
                request.getSourceLanguage(),
                request.getTargetLanguage()
            );
            
            // Create success response with constructor
            TranslationResponse response = new TranslationResponse(
                request.getDocumentId(),
                request.getTitle(),
                translatedTitle,
                request.getSourceLanguage(),
                request.getTargetLanguage(),
                Status.COMPLETED, // enum value
                null, // no error
                LocalDateTime.now().toString()
            );
            
            // Send response back
            kafkaProducer.sendTranslationResponse(response);
            
        } catch (Exception e) {
            logger.error("Failed to translate document {}: {}", request.getDocumentId(), e.getMessage());
            
            // Create failure response with constructor
            TranslationResponse errorResponse = new TranslationResponse(
                request.getDocumentId(),
                request.getTitle(),
                null, // no translated title
                request.getSourceLanguage(),
                request.getTargetLanguage(),
                Status.FAILED, // enum value
                e.getMessage(), // error message
                LocalDateTime.now().toString()
            );
            
            // Send error response
            kafkaProducer.sendTranslationResponse(errorResponse);
        }
    }
}
