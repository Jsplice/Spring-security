# ============================================================
# Stage 1 — Build
# Maven + JDK 21 to compile the application and produce the fat JAR.
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy dependency descriptors first to leverage Docker layer cache.
# Dependencies are only re-downloaded when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build, skipping unit tests (tests run in CI pipeline).
COPY src ./src
# Skip OWASP in image build — it pulls the NVD database (~300k records) and is meant for CI, not container builds.
RUN mvn clean package -DskipTests -B -Ddependency-check.skip=true

# ============================================================
# Stage 2 — Runtime
# Minimal JRE image — no build toolchain, no compiler, no Maven.
# Drastically reduces the attack surface of the final image.
# ============================================================
FROM eclipse-temurin:21-jre AS runtime

# Security: Never run as root inside a container.
# Create a dedicated, locked-down system user and group.
RUN groupadd --system appgroup && \
    useradd --system --no-create-home --gid appgroup appuser

WORKDIR /app

# Copy only the built artifact from the builder stage.
COPY --from=builder /build/target/*.jar app.jar

# Transfer ownership of the application file to the non-root user.
RUN chown appuser:appgroup app.jar

# Drop privileges: switch to the non-root user for all subsequent commands.
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
