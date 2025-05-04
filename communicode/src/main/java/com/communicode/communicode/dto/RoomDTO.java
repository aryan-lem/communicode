package com.communicode.communicode.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class RoomDTO {
    private String id;
    private String title;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Set<UserDTO> participants;
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Set<UserDTO> getParticipants() {
        return participants;
    }
    
    public void setParticipants(Set<UserDTO> participants) {
        this.participants = participants;
    }
}