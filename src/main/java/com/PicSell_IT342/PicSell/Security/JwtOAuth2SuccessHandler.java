package com.PicSell_IT342.PicSell.Security;

import com.PicSell_IT342.PicSell.Model.UserModel;
import com.PicSell_IT342.PicSell.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;

    public JwtOAuth2SuccessHandler(JwtEncoder jwtEncoder, UserRepository userRepository) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // Check and create user if not exists
        UserModel existingUser = userRepository.findByEmail(email);
        if (existingUser == null) {
            existingUser = new UserModel();
            existingUser.setUsername(email.split("@")[0]);
            existingUser.setEmail(email);
            existingUser.setPassword(""); // Oauth user doesn't need a password
            existingUser = userRepository.save(existingUser); // Save and assign
        }

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(existingUser.getUsername())
                .claim("userId", existingUser.getUserId())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        response.sendRedirect("/swagger-ui/index.html?token=" + token); // ✅ redirect to swagger
    }
}
