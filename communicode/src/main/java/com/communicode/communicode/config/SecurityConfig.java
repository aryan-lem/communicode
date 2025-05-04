package com.communicode.communicode.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.communicode.communicode.entity.User;
import com.communicode.communicode.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    private final UserService userService;

    @Autowired
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**", "/login/oauth2/code/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        // Remove loginPage - it conflicts with the flow
                        // Use standard Spring Security redirect handler
                        // .successHandler(oauth2AuthenticationSuccessHandler())
                        // .failureUrl("/api/auth/google/failure")
                        .defaultSuccessUrl("/login/oauth2/code/google", true) // Add this
                        .successHandler(oauth2AuthenticationSuccessHandler())
                        .failureHandler((request, response, exception) -> {
                            // Log the exception for debugging
                            exception.printStackTrace();
                            response.sendRedirect("/api/auth/google/failure");
                        }))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // Log successful logout
                            System.out.println("Logout successful for user: " +
                                    (authentication != null ? authentication.getName() : "unknown"));
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{\"message\":\"Successfully logged out\"}");
                            response.setContentType("application/json");
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            // Extract user info
            String googleId = oauth2User.getName();
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String picture = oauth2User.getAttribute("picture");

            // Create or update user
            User user = userService.createOrUpdateUser(email, name, googleId, picture);

            // Store user ID in session
            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getId());
            session.setAttribute("isNewUser", user.getUsername() == null);

            // Redirect to frontend
            response.sendRedirect("http://localhost:5173/auth/callback");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
                CommonOAuth2Provider.GOOGLE.getBuilder("google")
                        .clientId(googleClientId)
                        .clientSecret(googleClientSecret)
                        .scope("profile", "email")
                        // Add prompt parameter directly to authorization URI
                        .authorizationUri("https://accounts.google.com/o/oauth2/auth?prompt=select_account")
                        .build());
    }
}