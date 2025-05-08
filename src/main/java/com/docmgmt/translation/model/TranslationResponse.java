package com.docmgmt.translation.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TranslationResponse {
    private String documentId;
    private String originalTitle;
    private String translatedTitle;
    private String sourceLanguage;
    private String targetLanguage;
    private Status status;
    private String error;
    private String timestamp;
    
    public enum Status {
        COMPLETED,
        FAILED
    }
    
    // Explicit constructor that matches how it's being called in the code
    public TranslationResponse(String documentId, String originalTitle, String translatedTitle, 
                             String sourceLanguage, String targetLanguage, Status status, 
                             String error, String timestamp) {
        this.documentId = documentId;
        this.originalTitle = originalTitle;
        this.translatedTitle = translatedTitle;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.status = status;
        this.error = error;
        this.timestamp = timestamp;
    }
    
    // Adding explicit getter and setter methods to avoid Lombok issues
    
    public String getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    
    public String getOriginalTitle() {
        return originalTitle;
    }
    
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }
    
    public String getTranslatedTitle() {
        return translatedTitle;
    }
    
    public void setTranslatedTitle(String translatedTitle) {
        this.translatedTitle = translatedTitle;
    }
    
    public String getSourceLanguage() {
        return sourceLanguage;
    }
    
    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }
    
    public String getTargetLanguage() {
        return targetLanguage;
    }
    
    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Determines if the translation was successful
     * @return true if status is COMPLETED, false otherwise
     */
    public boolean isSuccessful() {
        return Status.COMPLETED.equals(status);
    }
}
