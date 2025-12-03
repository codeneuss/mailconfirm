package com.emailverification.service;

import com.emailverification.domain.VerificationToken;
import com.emailverification.repository.VerificationTokenRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class VerificationService {
    private static final Logger LOG = Logger.getLogger(VerificationService.class);

    @Inject
    VerificationTokenRepository tokenRepository;

    @Inject
    EmailService emailService;

    @ConfigProperty(name = "verification.token.expiry-hours")
    int expiryHours;

    public Uni<Void> initiateEmailVerification(String email) {
        LOG.infof("Initiating email verification for: %s", email);

        if (tokenRepository.existsByEmail(email)) {
            LOG.warnf("Pending verification already exists for email: %s", email);
            throw new IllegalStateException("A verification email has already been sent to this address");
        }

        VerificationToken token = new VerificationToken(email, expiryHours);
        tokenRepository.save(token);

        return emailService.sendVerificationEmail(token);
    }

    public String verifyEmail(String tokenValue) {
        LOG.infof("Verifying email with token: %s", tokenValue);

        VerificationToken token = tokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> {
                LOG.errorf("Invalid verification token: %s", tokenValue);
                return new IllegalArgumentException("Invalid verification token");
            });

        if (token.isExpired()) {
            LOG.warnf("Verification token expired for email: %s", token.getEmail());
            tokenRepository.deleteByToken(tokenValue);
            throw new IllegalStateException("Verification token has expired");
        }

        if (token.isVerified()) {
            LOG.warnf("Token already verified for email: %s", token.getEmail());
            throw new IllegalStateException("Email has already been verified");
        }

        token.setVerified(true);
        LOG.infof("Email successfully verified: %s", token.getEmail());

        return token.getEmail();
    }
}
