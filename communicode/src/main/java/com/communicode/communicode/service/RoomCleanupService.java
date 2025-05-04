package com.communicode.communicode.service;

import com.communicode.communicode.entity.EditorRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class RoomCleanupService {

    private final RoomService roomService;

    @Autowired
    public RoomCleanupService(RoomService roomService) {
        this.roomService = roomService;
    }
    
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    public void cleanupExpiredRooms() {
        LocalDateTime now = LocalDateTime.now();
        List<EditorRoom> expiredRooms = roomService.findRoomsExpiredBefore(now);
        
        for (EditorRoom room : expiredRooms) {
            roomService.deleteRoom(room.getId());
        }
    }
}