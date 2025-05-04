package com.communicode.communicode.controller;

import com.communicode.communicode.dto.UsernameRequest;
import com.communicode.communicode.dto.UserDTO;
import com.communicode.communicode.entity.User;
import com.communicode.communicode.exception.ResourceNotFoundException;
import com.communicode.communicode.exception.UsernameAlreadyExistsException;
import com.communicode.communicode.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/username")
    public ResponseEntity<?> setUsername(@RequestBody UsernameRequest request, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Not authenticated"));
        }
        
        try {
            User updatedUser = userService.setUsername(userId, request.getUsername());
            UserDTO userDto = new UserDTO(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getUsername(),
                updatedUser.getName(),
                updatedUser.getAvatarUrl(),
                false
            );
            
            return ResponseEntity.ok(Map.of("user", userDto));
        } catch (UsernameAlreadyExistsException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
        }
    }
}