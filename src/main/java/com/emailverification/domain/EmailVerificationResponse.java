package com.emailverification.domain;

public record EmailVerificationResponse(
    String message,
    String email
) {}
