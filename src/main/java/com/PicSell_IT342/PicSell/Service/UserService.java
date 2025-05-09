// src/main/java/com/PicSell_IT342/PicSell/Service/UserService.java
package com.PicSell_IT342.PicSell.Service;

import com.PicSell_IT342.PicSell.Model.UserModel;
import com.PicSell_IT342.PicSell.Repository.UserRepository;
import com.PicSell_IT342.PicSell.exception.CustomExceptions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtEncoder jwtEncoder;

    @Autowired
    public UserService(UserRepository userRepository, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
    }

    @Transactional
    public UserModel registerUser(UserModel user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new DuplicateResourceException("Username '" + user.getUsername() + "' already exists");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new DuplicateResourceException("Email '" + user.getEmail() + "' already exists");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("ROLE_USER"); // Default role with prefix
        return userRepository.save(user);
    }

    public Map<String, Object> login(String username, String password) {
        Optional<UserModel> userOpt = userRepository.findByUsernameOrEmail(username);
        if (userOpt.isEmpty() || !encoder.matches(password, userOpt.get().getPassword())) {
            log.warn("Login failed for identifier: {}", username);
            throw new AuthenticationException("Invalid username/email or password");
        }
        return generateTokenResponse(userOpt.get());
    }

    private Map<String, Object> generateTokenResponse(UserModel user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("Valid User object with ID is required for token generation.");
        }
        String subject = user.getUsername() != null ? user.getUsername() : user.getEmail();
        String role = user.getRole() != null ? (user.getRole().startsWith("ROLE_") ? user.getRole() : "ROLE_" + user.getRole()) : "ROLE_USER";

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .subject(subject)
                .claim("userId", user.getUserId())
                .claim("roles", role) // Send single role string
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
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

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        log.warn("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }

    @Transactional
    public UserModel updateUser(Long id, UserModel userDetails) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (userDetails.getUsername() != null && !userDetails.getUsername().isEmpty() && !userDetails.getUsername().equals(user.getUsername())) {
            Optional<UserModel> existingUserOpt = userRepository.findByUsernameOrEmail(userDetails.getUsername());
            if(existingUserOpt.isPresent() && !existingUserOpt.get().getUserId().equals(id)) {
                throw new DuplicateResourceException("Username '" + userDetails.getUsername() + "' already taken");
            }
            user.setUsername(userDetails.getUsername());
        }
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(encoder.encode(userDetails.getPassword()));
        }
        if (userDetails.getEmail() != null && !userDetails.getEmail().isEmpty() && !userDetails.getEmail().equals(user.getEmail())) {
            Optional<UserModel> existingUserOpt = userRepository.findByUsernameOrEmail(userDetails.getEmail());
            if(existingUserOpt.isPresent() && !existingUserOpt.get().getUserId().equals(id)) {
                throw new DuplicateResourceException("Email '" + userDetails.getEmail() + "' already taken");
            }
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getImagefile() != null) {
            user.setImagefile(userDetails.getImagefile());
        }

        log.info("Updating user details for ID: {}", id);
        return userRepository.save(user);
    }

    @Transactional
    public Map<String, Object> generateTokenFromEmail(String email) {
        Optional<UserModel> userOpt = userRepository.findByUsernameOrEmail(email);
        UserModel user;
        if (userOpt.isEmpty()) {
            log.info("User not found with email: {}. Attempting auto-registration...", email);
            UserModel newUser = new UserModel();
            newUser.setEmail(email);
            String potentialUsername = email.split("@")[0];
            if (userRepository.findByUsernameOrEmail(potentialUsername).isPresent()) {
                potentialUsername = email;
                if (userRepository.findByUsernameOrEmail(potentialUsername).isPresent()) {
                    throw new DuplicateResourceException("Cannot auto-register: generated username/email '" + potentialUsername + "' conflicts with an existing user.");
                }
            }
            newUser.setUsername(potentialUsername);
            newUser.setPassword(null);
            newUser.setRole("ROLE_USER");
            user = userRepository.save(newUser);
            log.info("Auto-registered new user from OAuth2: ID={}, Email={}", user.getUserId(), email);
        } else {
            user = userOpt.get();
        }
        return generateTokenResponse(user);
    }

    public Map<String, Object> generateTokenForAuthenticatedUser(String principalName) {
        UserModel user = userRepository.findByUsernameOrEmail(principalName)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for principal: " + principalName));
        return generateTokenResponse(user);
    }

    /**
     * Gets the internal database User ID for the currently authenticated user.
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Attempted to get user ID, but user is not authenticated.");
            return null;
        }

        Object principal = authentication.getPrincipal();
        Long userId = null;
        log.debug("Attempting to get UserID. Principal type: {}", (principal != null ? principal.getClass().getName() : "NULL"));

        try {
            if (principal instanceof Jwt jwt) {
                Object userIdClaim = jwt.getClaim("userId");
                if (userIdClaim instanceof Number) { userId = ((Number) userIdClaim).longValue(); }
                else if (userIdClaim instanceof String) { userId = Long.parseLong((String) userIdClaim); }
                log.debug("Extracted userId {} from JWT principal", userId);

            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                String identifier = userDetails.getUsername();
                Optional<UserModel> userOpt = userRepository.findByUsernameOrEmail(identifier);
                if (userOpt.isPresent()) {
                    userId = userOpt.get().getUserId();
                    log.debug("Found userId {} from UserDetails principal '{}'", userId, identifier);
                } else { log.error("Could not find user in DB for UserDetails principal: {}", identifier); }

            } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oauthUser) {
                String email = oauthUser.getAttribute("email");
                if (email != null) {
                    Optional<UserModel> userOpt = userRepository.findByUsernameOrEmail(email);
                    if (userOpt.isPresent()) {
                        userId = userOpt.get().getUserId();
                        log.debug("Found userId {} from OAuth2User principal with email '{}'", userId, email);
                    } else { log.error("Could not find user in DB for OAuth2User principal with email: {}", email); }
                } else { log.error("Could not get email attribute from OAuth2User principal to find user ID."); }

            } else if (principal instanceof String identifier) {
                Optional<UserModel> userOpt = userRepository.findByUsernameOrEmail(identifier);
                if (userOpt.isPresent()) {
                    userId = userOpt.get().getUserId();
                    log.debug("Found userId {} from String principal '{}'", userId, identifier);
                } else { log.error("Could not find user in DB for String principal: {}", identifier); }
            } else {
                log.error("Cannot extract userId from principal of unknown type: {}", principal.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Exception occurred while trying to extract userId from principal: {}", e.getMessage(), e);
        }

        if (userId == null) {
            log.error("Failed to determine user ID from principal.");
        }
        return userId;
    }
}