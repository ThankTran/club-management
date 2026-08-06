package com.example.demo.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BcryptPasswordHasherTest {

    private BcryptPasswordHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new BcryptPasswordHasher();
    }

    @Test
    void hash_GeneratesBcryptFormatHash() {
        String rawPassword = "mySecretPassword123";
        String hash = hasher.hash(rawPassword);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
    }

    @Test
    void matches_ValidatesCorrectPassword() {
        String rawPassword = "mySecretPassword123";
        String hash = hasher.hash(rawPassword);

        assertTrue(hasher.matches(rawPassword, hash));
        assertFalse(hasher.matches("wrongPassword", hash));
    }

    @Test
    void matches_ValidatesLegacySha256Hash() throws Exception {
        String rawPassword = "adminPassword";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] legacyHashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        String legacySha256Hash = Base64.getEncoder().encodeToString(legacyHashBytes);

        assertTrue(hasher.matches(rawPassword, legacySha256Hash));
        assertFalse(hasher.matches("wrongPassword", legacySha256Hash));
    }
}
