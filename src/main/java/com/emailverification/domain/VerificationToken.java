package com.emailverification.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class VerificationToken {
    private final String token;
    private final String email;
    private final LocalDateTime expiryDate;
    private boolean verified;

    public VerificationToken(String email, int expiryHours) {
        this.token = UUID.randomUUID().toString();
        this.email = email;
        this.expiryDate = LocalDateTime.now().plusHours(expiryHours);
        this.verified = false;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
