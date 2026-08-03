package com.example.demo.shared.security;

public interface PasswordHasher {
    String hash(String password);
    boolean matches(String rawPassword, String hashedPassword);
}
