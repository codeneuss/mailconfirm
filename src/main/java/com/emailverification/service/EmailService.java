package com.emailverification.service;

import com.emailverification.domain.VerificationToken;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.Location;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailService {
    private static final Logger LOG = Logger.getLogger(EmailService.class);

    @Inject
    ReactiveMailer mailer;

    @Inject
    @Location("verification-email.html")
    Template verificationEmailTemplate;

    @ConfigProperty(name = "app.base-url")
    String baseUrl;

    @ConfigProperty(name = "verification.token.expiry-hours")
    int expiryHours;

    public Uni<Void> sendVerificationEmail(VerificationToken token) {
        String verificationLink = buildVerificationLink(token.getToken());

        String htmlContent = verificationEmailTemplate
            .data("email", token.getEmail())
            .data("verificationLink", verificationLink)
            .data("expiryHours", expiryHours)
            .render();

        LOG.infof("Sending verification email to: %s", token.getEmail());

        return mailer.send(
            Mail.withHtml(token.getEmail(), "Please verify your email address", htmlContent)
        )
        .replaceWithVoid()
        .invoke(() -> LOG.infof("Verification email sent successfully to: %s", token.getEmail()))
        .onFailure().invoke(error -> LOG.errorf(error, "Failed to send verification email to: %s", token.getEmail()));
    }

    private String buildVerificationLink(String token) {
        return String.format("%s/api/verify?token=%s", baseUrl, token);
    }
}
