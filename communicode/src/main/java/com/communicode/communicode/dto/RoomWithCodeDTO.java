package com.communicode.communicode.dto;

public class RoomWithCodeDTO {
    private RoomDTO room;
    private String code;
    
    // Getters and setters
    public RoomDTO getRoom() {
        return room;
    }
    
    public void setRoom(RoomDTO room) {
        this.room = room;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
}