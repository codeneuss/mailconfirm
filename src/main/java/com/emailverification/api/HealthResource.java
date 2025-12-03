package com.emailverification.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/q/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Health", description = "Health check endpoints")
public class HealthResource {

    @GET
    @Path("/ready")
    @Operation(summary = "Readiness check", description = "Check if the application is ready to serve requests")
    public Map<String, String> ready() {
        return Map.of(
            "status", "UP",
            "checks", "ready"
        );
    }

    @GET
    @Path("/live")
    @Operation(summary = "Liveness check", description = "Check if the application is alive")
    public Map<String, String> live() {
        return Map.of(
            "status", "UP",
            "checks", "live"
        );
    }
}
