package com.communicode.communicode.dto;

public class UserDTO {
    private String id;
    private String email;
    private String username;
    private String name;
    private String avatarUrl;
    private boolean isNewUser;

    public UserDTO() {
    }

    public UserDTO(String id, String email, String username, String name, String avatarUrl, boolean isNewUser) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.isNewUser = isNewUser;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }
}