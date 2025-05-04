package com.communicode.communicode.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "editor_rooms")
public class EditorRoom {
    @Id
    private String id;
    
    private String title;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "room_participants",
        joinColumns = @JoinColumn(name = "room_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(7);
    }
    
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
    
    public Set<User> getParticipants() {
        return participants;
    }
    
    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }
    
    public void addParticipant(User user) {
        this.participants.add(user);
    }
    
    public void removeParticipant(User user) {
        this.participants.remove(user);
    }
}