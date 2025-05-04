package com.communicode.communicode.service;

import com.communicode.communicode.entity.User;
import com.communicode.communicode.exception.ResourceNotFoundException;
import com.communicode.communicode.exception.UsernameAlreadyExistsException;
import com.communicode.communicode.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createOrUpdateUser(String email, String name, String googleId, String avatarUrl) {
        Optional<User> existingUser = userRepository.findByGoogleId(googleId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setLastLogin(LocalDateTime.now());
            user.setName(name); // Update name in case it changed
            user.setAvatarUrl(avatarUrl); // Update avatar in case it changed
            return userRepository.save(user);
        } else {
            User newUser = new User(email, name, googleId, avatarUrl);
            return userRepository.save(newUser);
        }
    }

    @Transactional
    public User setUsername(String userId, String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setUsername(username);
        return userRepository.save(user);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}