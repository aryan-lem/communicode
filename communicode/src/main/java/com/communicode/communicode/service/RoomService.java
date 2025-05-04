package com.communicode.communicode.service;

import com.communicode.communicode.entity.EditorRoom;
import com.communicode.communicode.entity.User;
import com.communicode.communicode.exception.ResourceNotFoundException;
import com.communicode.communicode.repository.EditorRoomRepository;
import com.communicode.communicode.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    private final EditorRoomRepository roomRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoomService(EditorRoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EditorRoom createRoom(String title, String language, String customId) {
        EditorRoom room = new EditorRoom();

        // Generate a room ID if not provided
        String roomId = customId;
        if (roomId == null || roomId.isEmpty()) {
            roomId = generateUniqueRoomId();
        } else if (roomRepository.existsById(roomId)) {
            // If custom ID exists, generate a new one
            roomId = generateUniqueRoomId();
        }

        room.setId(roomId);
        room.setTitle(title);
        room.setLanguage(language != null ? language : "javascript");

        return roomRepository.save(room);
    }

    private String generateUniqueRoomId() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder builder = new StringBuilder();

        // Keep generating until we find a unique ID
        while (true) {
            builder.setLength(0);

            // Generate a 6-character random string
            for (int i = 0; i < 6; i++) {
                builder.append(chars.charAt(random.nextInt(chars.length())));
            }

            String id = builder.toString();
            if (!roomRepository.existsById(id)) {
                return id;
            }
        }
    }

    // Previous implementation of addUserToRoom method
    /*
     * @Transactional
     * public void addUserToRoom(String roomId, String userId) {
     * EditorRoom room = roomRepository.findById(roomId)
     * .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
     * 
     * User user = userRepository.findById(userId)
     * .orElseThrow(() -> new ResourceNotFoundException("User not found"));
     * 
     * room.addParticipant(user);
     * roomRepository.save(room);
     * }
     */

    @Transactional
    public void removeUserFromRoom(String roomId, String userId) {
        EditorRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        room.removeParticipant(user);
        roomRepository.save(room);
    }

    public Optional<EditorRoom> findById(String roomId) {
        return roomRepository.findById(roomId);
    }

    @Transactional
    public void deleteRoom(String roomId) {
        roomRepository.deleteById(roomId);
    }

    @Transactional
    public void updateRoomLanguage(String roomId, String language) {
        EditorRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        room.setLanguage(language);
        roomRepository.save(room);
    }

    // @Transactional
    // public void addUserToRoom(String roomId, String userId) {
    // try {
    // EditorRoom room = roomRepository.findById(roomId)
    // .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

    // User user = userRepository.findById(userId)
    // .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // // Check if user is already in room to avoid duplicates
    // if (room.getParticipants() == null) {
    // room.setParticipants(new HashSet<>());
    // }

    // // Add user and save
    // room.getParticipants().add(user);
    // roomRepository.save(room);

    // logger.info("Added user {} to room {}", userId, roomId);
    // } catch (Exception e) {
    // logger.error("Error adding user to room: ", e);
    // throw e; // Rethrow to allow controller to handle it
    // }
    // }
    @Transactional
    public void addUserToRoom(String roomId, String userId) {
        try {
            EditorRoom room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Initialize collection if null
            if (room.getParticipants() == null) {
                room.setParticipants(new HashSet<>());
            }

            // Check if user is already in the room to avoid duplicates
            boolean userAlreadyInRoom = room.getParticipants().stream()
                    .anyMatch(participant -> participant.getId().equals(userId));

            if (!userAlreadyInRoom) {
                // Add user and save only if not already a participant
                room.getParticipants().add(user);
                roomRepository.save(room);
                logger.info("Added user {} to room {}", userId, roomId);
            } else {
                logger.info("User {} is already in room {}, skipping add operation", userId, roomId);
            }
        } catch (ResourceNotFoundException e) {
            // Re-throw specific exceptions to allow controller to handle them
            logger.error("Resource not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error adding user to room: {}", e.getMessage(), e);
            // For other exceptions, wrap in a more specific exception if needed
            throw e;
        }
    }

    public List<EditorRoom> findRoomsExpiredBefore(LocalDateTime dateTime) {
        return roomRepository.findRoomsExpiredBefore(dateTime);
    }
}