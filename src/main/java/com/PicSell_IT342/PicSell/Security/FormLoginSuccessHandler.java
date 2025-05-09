package com.PicSell_IT342.PicSell.Security;

import com.PicSell_IT342.PicSell.Model.UserModel; // Import UserModel
import com.PicSell_IT342.PicSell.Repository.UserRepository; // Import UserRepository
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;
    private final ObjectMapper objectMapper;

    @Autowired
    public FormLoginSuccessHandler(UserRepository userRepository, JwtEncoder jwtEncoder, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        UserModel user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        Map<String, Object> tokenResponse = generateTokenResponse(user);

        System.out.println("[✅ FORM LOGIN] JWT Token generated for user: " + username);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), tokenResponse);
        response.getWriter().flush();
    }

    // Moved token generation logic here from UserService
    private Map<String, Object> generateTokenResponse(UserModel user) {
        if (user == null) throw new IllegalArgumentException("User object cannot be null for token generation.");
        if (user.getUserId() == null) throw new IllegalArgumentException("User ID cannot be null for token generation.");
        String subject = user.getUsername() != null ? user.getUsername() : user.getEmail();
        if (subject == null) throw new IllegalArgumentException("Username or Email cannot be null for token subject.");
        String role = user.getRole() != null ? user.getRole() : "ROLE_USER";

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder();
        builder.issuer("self");
        builder.subject(subject);
        builder.claim("userId", user.getUserId());
        builder.claim("roles", List.of(role));
        builder.issuedAt(Instant.now());
        builder.expiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        JwtClaimsSet claims = builder
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