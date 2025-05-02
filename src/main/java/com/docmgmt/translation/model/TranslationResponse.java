package com.docmgmt.translation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
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
}
