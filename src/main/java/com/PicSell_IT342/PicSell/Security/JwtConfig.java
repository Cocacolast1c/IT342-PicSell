package com.PicSell_IT342.PicSell.Security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator; // Import
import org.springframework.security.oauth2.core.OAuth2TokenValidator; // Import
import org.springframework.security.oauth2.jwt.*; // Import Jwt, JwtTimestampValidator, JwtIssuerValidator

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration; // Import Duration
import java.util.Arrays; // Import Arrays
import java.util.UUID;

@Configuration
public class JwtConfig {

    private KeyPair keyPair;

    @PostConstruct
    public void init() {
        this.keyPair = generateKeyPair();
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }

    private RSAPublicKey getPublicKey() {
        if (this.keyPair == null) { init(); }
        return (RSAPublicKey) this.keyPair.getPublic();
    }

    private RSAPrivateKey getPrivateKey() {
        if (this.keyPair == null) { init(); }
        return (RSAPrivateKey) this.keyPair.getPrivate();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        RSAKey rsaKey = new RSAKey.Builder(getPublicKey())
                .privateKey(getPrivateKey())
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(getPublicKey()).build();

        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator(Duration.ofSeconds(60)); // Allow 60 seconds skew

        OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator("self");


        OAuth2TokenValidator<Jwt> combinedValidators = new DelegatingOAuth2TokenValidator<>(
                timestampValidator,
                issuerValidator
        );

        jwtDecoder.setJwtValidator(combinedValidators);

        return jwtDecoder;
    }
}