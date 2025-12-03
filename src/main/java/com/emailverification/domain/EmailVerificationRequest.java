package com.emailverification.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequest(
    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be valid")
    String email
) {}
