package com.communicode.communicode.controller;

import com.communicode.communicode.dto.UserDTO;
import com.communicode.communicode.entity.User;
import com.communicode.communicode.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/google")
    public RedirectView redirectToGoogleAuth() {
        // The OAuth2 login process will handle this
        return new RedirectView("/oauth2/authorization/google");
    }

    @GetMapping("/google/callback")
    public RedirectView handleGoogleCallback(Authentication authentication, HttpSession session) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            
            String googleId = oauth2User.getName();
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String picture = oauth2User.getAttribute("picture");
            
            User user = userService.createOrUpdateUser(email, name, googleId, picture);
            
            // Store user ID in session
            session.setAttribute("userId", user.getId());
            session.setAttribute("isNewUser", user.getUsername() == null);
            
            // Redirect to the frontend
            return new RedirectView("http://localhost:5173/auth/callback");
        }
        
        // If authentication fails for some reason
        // Redirect to the login page with an error message
        
        return new RedirectView("http://localhost:5173/login?error=auth_failure");
    }

    @GetMapping("/google/failure")
    public RedirectView handleGoogleFailure() {
        return new RedirectView("http://localhost:5173/login?error=auth_failure");
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getSession(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        
        return userService.findById(userId).map(user -> {
            UserDTO userDto = new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                user.getAvatarUrl(),
                user.getUsername() == null
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("user", userDto);
            
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.ok(Map.of("authenticated", false)));
    }

    @PostMapping("/logout")
public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
    // Get the current authentication
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    if (auth != null) {
        // Log which user is logging out
        System.out.println("User logging out: " + auth.getName());
        
        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // Clear security context
        SecurityContextHolder.clearContext();
        
        // Clear cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }
    
    Map<String, String> result = new HashMap<>();
    result.put("message", "Successfully logged out");
    return ResponseEntity.ok(result);
}
}