# ── Stage 1: Build backend from monorepo root ───────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy backend build files first for cache efficiency
COPY backend/.mvn/ backend/.mvn/
COPY backend/mvnw backend/pom.xml ./backend/
RUN apk add --no-cache curl && chmod +x backend/mvnw
RUN cd backend && ./mvnw dependency:go-offline -q

# Copy backend sources and build the jar
COPY backend/src ./backend/src
RUN cd backend && ./mvnw package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Create a non-root user for runtime
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]