package com.communicode.communicode.dto;

public class LanguageChangeMessage {
    private String userId;
    private String language;
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
}