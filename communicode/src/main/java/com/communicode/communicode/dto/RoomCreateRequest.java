package com.communicode.communicode.dto;

public class RoomCreateRequest {
    private String title;
    private String language;
    private String customId;
    
    // Getters and setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getCustomId() {
        return customId;
    }
    
    public void setCustomId(String customId) {
        this.customId = customId;
    }
}