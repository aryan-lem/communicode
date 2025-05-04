package com.communicode.communicode.entity;

public class Participant {
    private String userId;
    private String username;
    private String joinedAt;
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }
}