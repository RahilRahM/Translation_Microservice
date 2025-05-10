package com.docmgmt.translation.service;

/**
 * Interface for translation services
 */
public interface TranslationServiceInterface {
    
    /**
     * Translates text from source language to target language
     * 
     * @param text The text to translate
     * @param sourceLanguage The source language code (e.g., "en")
     * @param targetLanguage The target language code (e.g., "es")
     * @return The translated text
     */
    String translateText(String text, String sourceLanguage, String targetLanguage);
}