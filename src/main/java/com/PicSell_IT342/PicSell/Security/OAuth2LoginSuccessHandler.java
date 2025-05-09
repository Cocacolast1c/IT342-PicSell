package com.PicSell_IT342.PicSell.Security;

import com.PicSell_IT342.PicSell.Model.UserModel;
import com.PicSell_IT342.PicSell.Repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;

    @Value("${frontend.auth.callback.url:http://localhost:3000/auth/google/callback}")
    private String frontendCallbackUrl;

    @Autowired
    // Updated constructor
    public OAuth2LoginSuccessHandler(UserRepository userRepository, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;

    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        if (email == null) {
            System.err.println("[❌ OAUTH2 LOGIN] Email not found in principal attributes.");
            response.sendRedirect(frontendCallbackUrl.replace("/auth/google/callback", "/login") + "?error=EmailNotFound");
            return;
        }

        UserModel user;
        try {
            user = findOrCreateUser(email, oauth2User); // Use helper method
        } catch (RuntimeException e) {
            System.err.println("[❌ OAUTH2 LOGIN] Error finding/creating user: " + e.getMessage());
            response.sendRedirect(frontendCallbackUrl.replace("/auth/google/callback", "/login") + "?error=UserProcessingFailed");
            return;
        }


        Map<String, Object> tokenResponse = generateTokenResponse(user);
        String token = (String) tokenResponse.get("token");
        String userId = tokenResponse.get("userId").toString();
        String username = (String) tokenResponse.get("username");
        String userEmail = (String) tokenResponse.get("email");
        String roles = (String) tokenResponse.get("roles");


        String redirectUrl = UriComponentsBuilder.fromHttpUrl(frontendCallbackUrl)
                .queryParam("token", token)
                .queryParam("userId", userId)
                .queryParam("username", username)
                .queryParam("email", userEmail)
                .queryParam("roles", roles)
                .encode()
                .toUriString();

        System.out.println("[✅ OAUTH2 LOGIN] Redirecting to: " + frontendCallbackUrl); // Log base URL only for clarity
        response.sendRedirect(redirectUrl);
    }

    private UserModel findOrCreateUser(String email, OAuth2User oauth2User) {
        UserModel user = userRepository.findByEmail(email);
        if (user == null) {
            System.out.println("[ℹ️ OAUTH2 LOGIN] User not found, attempting auto-registration for: " + email);
            user = new UserModel();
            user.setEmail(email);

            String name = oauth2User.getAttribute("name");
            String username = name != null ? name.replaceAll("[^a-zA-Z0-9]", "") : email.split("@")[0]; // Simple username generation

            if (userRepository.findByUsername(username) != null) {
                if (userRepository.findByUsername(email) == null) {
                    username = email;
                } else {
                    System.err.println("[❌ OAUTH2 LOGIN] Cannot auto-register, username conflict for: " + username + " and " + email);
                    throw new RuntimeException("Username conflict during auto-registration.");
                }
            }

            user.setUsername(username);
            user.setPassword(null);
            user.setRole("ROLE_USER");
            user = userRepository.save(user);
            System.out.println("[✅ OAUTH2 LOGIN] Auto-registered new user: " + user.getUsername() + " (ID: " + user.getUserId() + ")");
        }
        return user;
    }


    private Map<String, Object> generateTokenResponse(UserModel user) {
        if (user == null) throw new IllegalArgumentException("User object cannot be null for token generation.");
        if (user.getUserId() == null) throw new IllegalArgumentException("User ID cannot be null for token generation.");
        String subject = user.getUsername() != null ? user.getUsername() : user.getEmail();
        if (subject == null) throw new IllegalArgumentException("Username or Email cannot be null for token subject.");
        String role = user.getRole() != null ? user.getRole() : "ROLE_USER"; // Default role

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .subject(subject)
                .claim("userId", user.getUserId())
                .claim("roles", List.of(role))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS)) // Standard 1-hour expiry
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("roles", role);
        return result;
    }
}