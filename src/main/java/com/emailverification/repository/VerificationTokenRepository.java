package com.emailverification.repository;

import com.emailverification.domain.VerificationToken;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class VerificationTokenRepository {
    private final Map<String, VerificationToken> tokens = new ConcurrentHashMap<>();

    public void save(VerificationToken token) {
        tokens.put(token.getToken(), token);
    }

    public Optional<VerificationToken> findByToken(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    public void deleteByToken(String token) {
        tokens.remove(token);
    }

    public boolean existsByEmail(String email) {
        return tokens.values().stream()
            .anyMatch(token -> token.getEmail().equals(email) && !token.isVerified());
    }
}
