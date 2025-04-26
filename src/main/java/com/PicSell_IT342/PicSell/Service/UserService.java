package com.PicSell_IT342.PicSell.Service;

import com.PicSell_IT342.PicSell.Model.UserModel;
import com.PicSell_IT342.PicSell.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtEncoder jwtEncoder;

    @Autowired
    public UserService(UserRepository userRepository, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
    }

    public UserModel registerUser(UserModel user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    public Map<String, Object> login(String username, String password) {
        UserModel user = userRepository.findByUsername(username);
        if (user == null || !encoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return generateTokenResponse(user);
    }

    private Map<String, Object> generateTokenResponse(UserModel user) {
        // Check for nulls and throw meaningful exceptions if encountered
        if (user == null) {
            throw new IllegalArgumentException("User object is null.");
        }
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("User ID is null.");
        }
        if (user.getUsername() == null) {
            throw new IllegalArgumentException("Username is null.");
        }
        if (user.getRole() == null) {
            throw new IllegalArgumentException("Role is null.");
        }
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .claim("userId", user.getUserId())
                .claim("roles", List.of(user.getRole()))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("roles", user.getRole());
        return result;

    }

    public String deleteUser(Long id) {
        userRepository.deleteById(id);
        return "User deleted";
    }

    public String updateUser(Long id, UserModel userDetails) {
        UserModel user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setUsername(userDetails.getUsername());
            user.setPassword(encoder.encode(userDetails.getPassword()));
            user.setEmail(userDetails.getEmail());
            userRepository.save(user);
            return "User updated";
        }
        return null;
    }

    public Map<String, Object> generateTokenFromEmail(String email) {
        UserModel user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found for email: " + email);
        }

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .claim("userId", user.getUserId())
                .claim("roles", List.of(user.getRole()))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return Map.of(
                "token", token,
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "roles", user.getRole()
        );
    }
}