package com.communicode.communicode.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_snapshots")
public class CodeSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "room_id")
    private String roomId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private LocalDateTime savedAt;
    
    private Long version;
    
    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getSavedAt() {
        return savedAt;
    }
    
    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
}