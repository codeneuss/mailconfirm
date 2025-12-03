####
# This Dockerfile is used to build the Quarkus application
####

# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 quarkus && \
    adduser -u 1001 -G quarkus -s /bin/sh -D quarkus

# Copy the jar file from build stage
COPY --from=build /build/target/quarkus-app/lib/ /app/lib/
COPY --from=build /build/target/quarkus-app/*.jar /app/
COPY --from=build /build/target/quarkus-app/app/ /app/app/
COPY --from=build /build/target/quarkus-app/quarkus/ /app/quarkus/

# Set permissions
RUN chown -R quarkus:quarkus /app

# Switch to non-root user
USER quarkus

# Expose port
EXPOSE 8080

# Set the Java options
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Run the application
CMD ["java", "-jar", "quarkus-run.jar"]
