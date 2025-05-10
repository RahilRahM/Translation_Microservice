package com.docmgmt.translation.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for loading environment variables from .env file
 */
@Configuration
public class EnvConfig {

    private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);
    
    @Bean
    public Dotenv dotenv(ConfigurableEnvironment environment) {
        // Check if .env file exists
        File envFile = new File(".env");
        if (!envFile.exists()) {
            logger.warn(".env file not found in the current directory");
            return null;
        }
        
        try {
            // Load .env file
            Dotenv dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();
            logger.info(".env file loaded successfully");

            // Add .env variables to Spring environment
            Map<String, Object> envMap = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                envMap.put(entry.getKey(), entry.getValue());
                // Explicitly set system properties for key environment variables
                if (entry.getKey().equals("GEMINI_API_KEY")) {
                    System.setProperty("app.gemini.api-key", entry.getValue());
                    logger.info("Loaded Gemini API key from .env file");
                }
            });
            
            // Add property source
            MapPropertySource propertySource = new MapPropertySource("dotenvProperties", envMap);
            environment.getPropertySources().addFirst(propertySource);
            
            return dotenv;
        } catch (Exception e) {
            logger.error("Error loading .env file: {}", e.getMessage(), e);
            return null;
        }
    }
}