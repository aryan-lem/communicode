package com.communicode.communicode.controller;

import com.communicode.communicode.dto.CodeChangeMessage;
import com.communicode.communicode.dto.LanguageChangeMessage;
import com.communicode.communicode.dto.ParticipantMessage;
import com.communicode.communicode.entity.EditorRoom;
import com.communicode.communicode.entity.Participant;
import com.communicode.communicode.entity.User;
import com.communicode.communicode.service.CodeService;
import com.communicode.communicode.service.RoomService;
import com.communicode.communicode.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.dao.DataIntegrityViolationException;

@Controller
public class CodeEditorController {
    private static final Logger logger = LoggerFactory.getLogger(CodeEditorController.class);

    @Autowired
    private RoomService roomService;

    @Autowired
    private CodeService codeService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // @MessageMapping("/rooms/{roomId}/code-change")
    // @SendTo("/topic/rooms/{roomId}")
    // public CodeChangeMessage codeChange(@DestinationVariable String roomId,
    // CodeChangeMessage message) {
    // logger.info("Received code change for room " + roomId);
    // codeService.saveCode(roomId, message.getContent(),
    // System.currentTimeMillis());
    // return message;
    // }
    @MessageMapping("/rooms/{roomId}/code-change")
    @SendTo("/topic/rooms/{roomId}")
    public CodeChangeMessage codeChange(@DestinationVariable String roomId, CodeChangeMessage message) {
        logger.info("Received code change for room " + roomId);
        try {
            codeService.saveCode(roomId, message.getContent(), System.currentTimeMillis());
            return message;
        } catch (Exception e) {
            logger.error("Error handling code change: {}", e.getMessage(), e);
            // Still return the message so other users get the update
            return message;
        }
    }

    @MessageMapping("/rooms/{roomId}/language-change")
    @SendTo("/topic/rooms/{roomId}/language")
    public LanguageChangeMessage languageChange(@DestinationVariable String roomId, LanguageChangeMessage message) {
        logger.info("Language changed to " + message.getLanguage() + " for room " + roomId);
        roomService.updateRoomLanguage(roomId, message.getLanguage());
        return message;
    }

    // @MessageMapping("/rooms/{roomId}/join")
    // public void joinRoom(@DestinationVariable String roomId, ParticipantMessage
    // message) {
    // logger.info("User joined room: " + roomId + ", userId: " +
    // message.getUserId());

    // try {
    // // Create a transaction template for explicit transaction management
    // TransactionTemplate transactionTemplate = new
    // TransactionTemplate(transactionManager);

    // // Execute in transaction and collect participants
    // List<Map<String, Object>> participants = transactionTemplate.execute(status
    // -> {
    // try {
    // // Add user to room
    // roomService.addUserToRoom(roomId, message.getUserId());

    // // Fetch room with participants in the same transaction
    // EditorRoom room = roomService.findById(roomId).orElse(null);
    // if (room == null) {
    // return new ArrayList<>();
    // }

    // // Convert participants to a list of maps while still in transaction
    // List<Map<String, Object>> participantList = new ArrayList<>();
    // for (User user : room.getParticipants()) {
    // Map<String, Object> participantMap = new HashMap<>();
    // participantMap.put("userId", user.getId());
    // participantMap.put("username",
    // user.getUsername() != null ? user.getUsername() : user.getName());
    // participantList.add(participantMap);
    // }
    // return participantList;

    // } catch (Exception e) {
    // logger.error("Error adding user to room database: {}", e.getMessage());
    // status.setRollbackOnly();
    // return new ArrayList<>();
    // }
    // });
    @MessageMapping("/rooms/{roomId}/join")
    public void joinRoom(@DestinationVariable String roomId, ParticipantMessage message) {
        String userId = message.getUserId();
        logger.info("Processing join request for room: {} by user: {}", roomId, userId);

        try {
            // Use a synchronized block keyed on the userId+roomId to prevent concurrent
            // joins
            synchronized (("room-join-" + roomId + "-" + userId).intern()) {
                // Execute in transaction
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

                List<Map<String, Object>> participants = transactionTemplate.execute(status -> {
                    try {
                        // Try to add user to room - the method already checks for duplicates
                        roomService.addUserToRoom(roomId, userId);

                        // Get updated participants list
                        EditorRoom room = roomService.findById(roomId).orElse(null);
                        if (room == null) {
                            return new ArrayList<>();
                        }

                        // Build participant list
                        List<Map<String, Object>> participantList = new ArrayList<>();
                        if (room.getParticipants() != null) {
                            for (User user : room.getParticipants()) {
                                Map<String, Object> participantMap = new HashMap<>();
                                participantMap.put("userId", user.getId());
                                participantMap.put("username",
                                        user.getUsername() != null ? user.getUsername() : user.getName());
                                participantList.add(participantMap);
                            }
                        }
                        return participantList;
                    } catch (Exception e) {
                        if (e instanceof DataIntegrityViolationException) {
                            // This is a duplicate join, just log and continue
                            logger.info("User {} is already in room {}, ignoring duplicate join", userId, roomId);

                            // Still need to return the participants
                            EditorRoom room = roomService.findById(roomId).orElse(null);
                            if (room != null && room.getParticipants() != null) {
                                List<Map<String, Object>> participantList = new ArrayList<>();
                                for (User user : room.getParticipants()) {
                                    Map<String, Object> participantMap = new HashMap<>();
                                    participantMap.put("userId", user.getId());
                                    participantMap.put("username",
                                            user.getUsername() != null ? user.getUsername() : user.getName());
                                    participantList.add(participantMap);
                                }
                                return participantList;
                            }
                        } else {
                            logger.error("Error handling room join: {}", e.getMessage(), e);
                        }
                        status.setRollbackOnly();
                        return new ArrayList<>();
                    }
                });

                // Always ensure current user is included
                boolean userIncluded = false;
                for (Map<String, Object> p : participants) {
                    if (userId.equals(p.get("userId"))) {
                        userIncluded = true;
                        break;
                    }
                }

                if (!userIncluded) {
                    Map<String, Object> participantMap = new HashMap<>();
                    participantMap.put("userId", userId);
                    participantMap.put("username", message.getUsername());
                    participants.add(participantMap);
                }

                // Broadcast participants list
                messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/participants", participants);
                logger.info("Sent {} participants for room {}", participants.size(), roomId);
            }
        } catch (Exception e) {
            logger.error("Unexpected error in joinRoom: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/rooms/{roomId}/leave")
    public void leaveRoom(@DestinationVariable String roomId, ParticipantMessage message) {
        logger.info("User left room: " + roomId + ", userId: " + message.getUserId());

        try {
            // Remove user from room
            roomService.removeUserFromRoom(roomId, message.getUserId());

            // Create participant data to broadcast
            List<Participant> participants = new ArrayList<>();
            roomService.findById(roomId).ifPresent(room -> {
                room.getParticipants().forEach(p -> {
                    Participant part = new Participant();
                    part.setUserId(p.getId());
                    part.setUsername(p.getUsername());
                    participants.add(part);
                });
            });

            // Send updated participant list to all subscribers
            messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/participants", participants);
        } catch (Exception e) {
            logger.error("Error removing user from room", e);
        }
    }
}