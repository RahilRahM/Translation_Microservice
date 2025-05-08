package com.docmgmt.translation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
import org.springframework.kafka.event.ConsumerStoppedEvent;
import org.springframework.kafka.event.ConsumerFailedToStartEvent;

import javax.annotation.PostConstruct;

@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaRetryConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaRetryConfig.class);
    private int maxRetries = 5;
    private int retryCount = 0;
    
    @EventListener(ApplicationStartedEvent.class)
    public void init() {
        // Set Kafka client properties to avoid excessive reconnection attempts
        System.setProperty("reconnect.backoff.ms", "5000");
        System.setProperty("reconnect.backoff.max.ms", "30000");
        logger.info("Kafka retry configuration initialized");
    }
    
    @EventListener(ConsumerFailedToStartEvent.class)
    public void handleConsumerFailedToStart(ConsumerFailedToStartEvent event) {
        logger.warn("Kafka consumer failed to start: {}", event.toString());
        if (++retryCount > maxRetries) {
            logger.error("Max Kafka connection retries ({}) exceeded. Consider disabling Kafka with app.kafka.enabled=false", maxRetries);
        }
    }
    
    @EventListener(ConsumerStoppedEvent.class)
    public void handleConsumerStopped(ConsumerStoppedEvent event) {
        logger.info("Kafka consumer stopped: {}", event.getReason());
    }
    
    @EventListener(ListenerContainerIdleEvent.class)
    public void handleIdle(ListenerContainerIdleEvent event) {
        logger.debug("Kafka consumer idle: {}", event.getListenerId());
    }
    
    @PostConstruct
    public void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application shutdown detected, stopping Kafka consumers");
            // Additional cleanup if needed
        }));
    }
}
