package com.emailverification.api;

import com.emailverification.domain.EmailVerificationRequest;
import com.emailverification.domain.EmailVerificationResponse;
import com.emailverification.service.VerificationService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Email Verification", description = "Email verification operations")
public class EmailVerificationResource {
    private static final Logger LOG = Logger.getLogger(EmailVerificationResource.class);

    @Inject
    VerificationService verificationService;

    @POST
    @Path("/send-verification")
    @Operation(
        summary = "Send verification email",
        description = "Sends a verification email to the provided email address with a confirmation link"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "202",
            description = "Verification email sent successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = EmailVerificationResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid email address or verification already pending"
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public Uni<Response> sendVerificationEmail(
        @Valid EmailVerificationRequest request
    ) {
        LOG.infof("Received verification request for email: %s", request.email());

        return verificationService.initiateEmailVerification(request.email())
            .map(v -> Response.accepted(
                new EmailVerificationResponse(
                    "Verification email sent successfully. Please check your inbox.",
                    request.email()
                )
            ).build())
            .onFailure().recoverWithItem(ex -> {
                LOG.errorf(ex, "Error sending verification email to: %s", request.email());
                if (ex instanceof IllegalStateException) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new EmailVerificationResponse(ex.getMessage(), request.email()))
                        .build();
                }
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new EmailVerificationResponse("Failed to send verification email", request.email()))
                    .build();
            });
    }

    @GET
    @Path("/verify")
    @Produces(MediaType.TEXT_HTML)
    @Operation(
        summary = "Verify email address",
        description = "Verifies the email address using the token sent via email"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Email verified successfully",
            content = @Content(mediaType = MediaType.TEXT_HTML)
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid or expired token"
        )
    })
    public Response verifyEmail(
        @Parameter(
            description = "Verification token from the email",
            required = true
        )
        @QueryParam("token") String token
    ) {
        if (token == null || token.isBlank()) {
            LOG.warn("Verification attempt with missing token");
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(buildErrorHtml("Invalid Request", "Verification token is missing."))
                .build();
        }

        try {
            String email = verificationService.verifyEmail(token);
            LOG.infof("Email verified successfully: %s", email);
            return Response.ok(buildSuccessHtml(email)).build();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            LOG.warnf("Verification failed: %s", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(buildErrorHtml("Verification Failed", ex.getMessage()))
                .build();
        }
    }

    private String buildSuccessHtml(String email) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Email Verified</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                    }
                    .container {
                        background: white;
                        border-radius: 10px;
                        padding: 40px;
                        max-width: 500px;
                        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
                        text-align: center;
                    }
                    .success-icon {
                        width: 80px;
                        height: 80px;
                        border-radius: 50%%;
                        background-color: #10b981;
                        margin: 0 auto 20px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .success-icon::before {
                        content: "✓";
                        color: white;
                        font-size: 50px;
                        font-weight: bold;
                    }
                    h1 {
                        color: #1f2937;
                        margin-bottom: 10px;
                    }
                    p {
                        color: #6b7280;
                        font-size: 16px;
                        line-height: 1.6;
                    }
                    .email {
                        color: #667eea;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-icon"></div>
                    <h1>Email Verified!</h1>
                    <p>Your email address <span class="email">%s</span> has been successfully verified.</p>
                    <p>You can now close this window.</p>
                </div>
            </body>
            </html>
            """.formatted(email);
    }

    private String buildErrorHtml(String title, String message) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verification Error</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%);
                    }
                    .container {
                        background: white;
                        border-radius: 10px;
                        padding: 40px;
                        max-width: 500px;
                        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
                        text-align: center;
                    }
                    .error-icon {
                        width: 80px;
                        height: 80px;
                        border-radius: 50%%;
                        background-color: #ef4444;
                        margin: 0 auto 20px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .error-icon::before {
                        content: "✕";
                        color: white;
                        font-size: 50px;
                        font-weight: bold;
                    }
                    h1 {
                        color: #1f2937;
                        margin-bottom: 10px;
                    }
                    p {
                        color: #6b7280;
                        font-size: 16px;
                        line-height: 1.6;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="error-icon"></div>
                    <h1>%s</h1>
                    <p>%s</p>
                </div>
            </body>
            </html>
            """.formatted(title, message);
    }
}
