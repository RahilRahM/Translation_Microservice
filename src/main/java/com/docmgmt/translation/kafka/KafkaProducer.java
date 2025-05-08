package com.docmgmt.translation.kafka;

import com.docmgmt.translation.model.TranslationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${app.topics.translation-response}")
    private String translationResponseTopic;
    
    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void sendTranslationResponse(TranslationResponse response) {
        logger.info("Sending translation response for document ID: {}", response.getDocumentId());
        
        try {
            // Send to document.translation.response topic
            kafkaTemplate.send(translationResponseTopic, response.getDocumentId(), response);
            logger.info("Translation response sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send translation response: {}", e.getMessage(), e);
        }
    }
}
