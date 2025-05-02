package com.docmgmt.translation.kafka;

import com.docmgmt.translation.model.TranslationRequest;
import com.docmgmt.translation.model.TranslationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.topics.translation-response}")
    private String translationResponseTopic;
    
    @Value("${app.topics.dlq}")
    private String dlqTopic;
    
    public void sendTranslationResponse(TranslationResponse response) {
        try {
            log.info("Sending translation response for document: {}", response.getDocumentId());
            kafkaTemplate.send(translationResponseTopic, response.getDocumentId(), response);
        } catch (Exception e) {
            log.error("Error sending translation response: {}", e.getMessage());
        }
    }
    
    public void sendToDLQ(TranslationRequest request, Exception error) {
        try {
            log.info("Sending failed message to DLQ for document: {}", request.getDocumentId());
            
            Map<String, Object> dlqMessage = new HashMap<>();
            dlqMessage.put("originalMessage", request);
            dlqMessage.put("error", Map.of(
                "message", error.getMessage(),
                "timestamp", Instant.now().toString()
            ));
            
            kafkaTemplate.send(dlqTopic, request.getDocumentId(), dlqMessage);
        } catch (Exception e) {
            log.error("Error sending to DLQ: {}", e.getMessage());
        }
    }
}
