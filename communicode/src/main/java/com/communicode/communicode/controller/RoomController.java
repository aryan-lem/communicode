package com.communicode.communicode.controller;

import com.communicode.communicode.dto.RoomCreateRequest;
import com.communicode.communicode.dto.RoomDTO;
import com.communicode.communicode.dto.RoomWithCodeDTO;
import com.communicode.communicode.dto.UserDTO;
import com.communicode.communicode.entity.CodeSnapshot;
import com.communicode.communicode.entity.EditorRoom;
import com.communicode.communicode.entity.User;
import com.communicode.communicode.exception.ResourceNotFoundException;
import com.communicode.communicode.service.CodeService;
import com.communicode.communicode.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);
    private final RoomService roomService;
    private final CodeService codeService;

    @Autowired
    public RoomController(RoomService roomService, CodeService codeService) {
        this.roomService = roomService;
        this.codeService = codeService;
    }

    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomCreateRequest request, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        EditorRoom room = roomService.createRoom(
                request.getTitle(),
                request.getLanguage(),
                request.getCustomId());

        // Initialize empty code for the room
        codeService.initializeCode(room.getId(), room.getLanguage());

        // Add the creator to the room
        roomService.addUserToRoom(room.getId(), userId);

        return ResponseEntity.ok(convertToDTO(room));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoom(@PathVariable String roomId, Principal principal) {
        try {
            // Log the request
            logger.info("Fetching room: {} by user: {}", roomId,
                    principal != null ? principal.getName() : "anonymous");

            // Check if room exists
            EditorRoom room = roomService.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

            // Get latest code snapshot
            String content = "";
            // try {
            //     content = codeService.getLatestCodeContent(roomId);
            // } catch (Exception e) {
            //     logger.warn("Could not fetch code content for room {}: {}", roomId, e.getMessage());
            //     // Continue with empty content instead of failing
            // }
            try {
                CodeSnapshot latestSnapshot = codeService.getLatestSnapshot(roomId);
                content = latestSnapshot.getContent();
            } catch (ResourceNotFoundException e) {
                logger.warn("No code snapshot found for room {}", roomId);
                content = ""; // Default to empty content
            }

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("id", room.getId());
            response.put("title", room.getTitle());
            response.put("language", room.getLanguage());
            response.put("createdAt", room.getCreatedAt());
            response.put("expiresAt", room.getExpiresAt());
            response.put("content", content);

            // Add participants array (even if empty)
            List<Map<String, Object>> participantsList = new ArrayList<>();
            if (room.getParticipants() != null) {
                for (User user : room.getParticipants()) {
                    Map<String, Object> participantMap = new HashMap<>();
                    participantMap.put("userId", user.getId());
                    participantMap.put("username", user.getUsername() != null ? user.getUsername() : user.getName());
                    participantsList.add(participantMap);
                }
            }
            response.put("participants", participantsList);

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.warn("Room not found: {}", roomId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Log the full stack trace for troubleshooting
            logger.error("Error retrieving room: {}", roomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve room details: " + e.getMessage()));
        }
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<RoomDTO> joinRoom(@PathVariable String roomId, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        roomService.addUserToRoom(roomId, userId);

        EditorRoom room = roomService.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        return ResponseEntity.ok(convertToDTO(room));
    }

    private RoomDTO convertToDTO(EditorRoom room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setTitle(room.getTitle());
        dto.setLanguage(room.getLanguage());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setExpiresAt(room.getExpiresAt());

        // Convert participants to DTOs
        dto.setParticipants(room.getParticipants().stream()
                .map(user -> {
                    UserDTO userDto = new UserDTO();
                    userDto.setId(user.getId());
                    userDto.setUsername(user.getUsername());
                    userDto.setName(user.getName());
                    userDto.setAvatarUrl(user.getAvatarUrl());
                    return userDto;
                })
                .collect(Collectors.toSet()));

        return dto;
    }
}