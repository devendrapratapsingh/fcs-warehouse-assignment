# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 — Builder
#
# Uses the official Maven image with Eclipse Temurin JDK 17.
# Maven downloads all dependencies into a Docker layer cache so that
# subsequent builds skip the download step when only source changes.
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 1. Copy dependency descriptors first — this layer is only invalidated when
#    pom.xml changes, not when source files change.
COPY pom.xml ./

# 2. Pre-download all compile/runtime dependencies (skips tests and source).
#    The empty src tree satisfies the Maven lifecycle without real sources.
RUN mvn dependency:go-offline -q --no-transfer-progress

# 3. Copy the rest of the project and run the full package phase.
#    -DskipTests=true skips test execution but still compiles test sources.
#    Remove -DskipTests to run the full test suite during the image build.
COPY src ./src

RUN mvn package -DskipTests=true --no-transfer-progress -q

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 — Runtime
#
# Uses the Eclipse Temurin JRE 17 on Alpine — the smallest official JRE image
# (~85 MB base vs ~240 MB for the UBI8 image used in Dockerfile.jvm).
# Only the compiled application layers are copied from the builder stage;
# the JDK, Maven cache, and build artefacts are discarded.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

# Non-root user for security hardening
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /deployments

# Quarkus fast-jar layout — four distinct layers so that only the app/ layer
# (your code) is invalidated on each rebuild; lib/ and quarkus/ stay cached.
COPY --from=builder --chown=appuser:appgroup /build/target/quarkus-app/lib/     ./lib/
COPY --from=builder --chown=appuser:appgroup /build/target/quarkus-app/*.jar    ./
COPY --from=builder --chown=appuser:appgroup /build/target/quarkus-app/app/     ./app/
COPY --from=builder --chown=appuser:appgroup /build/target/quarkus-app/quarkus/ ./quarkus/

EXPOSE 8080

# Docker-native health check using Quarkus SmallRye Health liveness endpoint.
# Starts after 30 s to allow JVM warm-up; retried every 10 s with a 5 s timeout.
# Requires: quarkus-smallrye-health on the classpath (added in pom.xml).
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -q -O- http://localhost:8080/q/health/live || exit 1

ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 \
               -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /deployments/quarkus-run.jar"]
