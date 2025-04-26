package com.PicSell_IT342.PicSell.Controller;

import com.PicSell_IT342.PicSell.Model.UserModel;
import com.PicSell_IT342.PicSell.Repository.UserRepository;
import com.PicSell_IT342.PicSell.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
@RequestMapping("/token")
public class TokenController {

    private final UserService userService;
    private final JwtDecoder jwtDecoder;

    public TokenController(UserService userService, JwtDecoder jwtDecoder) {
        this.userService = userService;
        this.jwtDecoder = jwtDecoder;
    }

    // Used to validate a token passed from frontend
    @PostMapping("/test")
    public ResponseEntity<?> testToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "subject", jwt.getSubject(),
                    "userId", jwt.getClaim("userId"),
                    "issuedAt", jwt.getIssuedAt(),
                    "expiresAt", jwt.getExpiresAt()
            ));
        } catch (JwtException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", ex.getMessage()
            ));
        }
    }

    // Used to regenerate a token for an authenticated OAuth2 user
    @GetMapping("/api/token")
    public ResponseEntity<?> getToken(@AuthenticationPrincipal OAuth2User user) {
        if (user == null || user.getAttribute("email") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized or email missing"));
        }
        return ResponseEntity.ok(userService.generateTokenFromEmail(user.getAttribute("email")));
    }
}

