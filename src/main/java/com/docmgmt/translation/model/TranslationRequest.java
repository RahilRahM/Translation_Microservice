package com.docmgmt.translation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationRequest {
    private String documentId;
    private String title;
    private String sourceLanguage = "en";
    private String targetLanguage = "es";
}
