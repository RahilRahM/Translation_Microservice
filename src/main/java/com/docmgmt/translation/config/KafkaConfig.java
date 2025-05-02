package com.docmgmt.translation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${app.kafka.enabled:true}")
    private boolean kafkaEnabled;

    /**
     * This bean allows Spring to conditionally enable/disable Kafka components
     * based on the app.kafka.enabled property
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
    /**
     * Method to check if Kafka is enabled for logging purposes
     */
    @Bean
    public boolean isKafkaEnabled() {
        return kafkaEnabled;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        
        // Configure error handler with retry
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            new FixedBackOff(5000L, 3));
        factory.setCommonErrorHandler(errorHandler);
        
        return factory;
    }
}
