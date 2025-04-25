package com.PicSell_IT342.PicSell.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/token")
public class TokenController {

    private final JwtDecoder jwtDecoder;

    public TokenController(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

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
}
