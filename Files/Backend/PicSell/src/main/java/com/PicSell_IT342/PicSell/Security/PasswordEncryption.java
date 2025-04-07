package com.PicSell_IT342.PicSell.Security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncryption {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Hash the raw password
    public static String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    // Check if the raw password matches the hashed password
    public static boolean matches(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}