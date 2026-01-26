# Stage 1: Build
FROM gradle:8.5-jdk17-alpine AS build

WORKDIR /app

# Copy Gradle files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build the application using bootJar to generate executable JAR
RUN gradle bootJar --no-daemon -x test

# Debug: List files in build/libs to verify JAR generation
RUN echo "=== Arquivos gerados em build/libs ===" && \
    ls -lah /app/build/libs/ && \
    echo "" && \
    echo "=== Verificando JARs encontrados ===" && \
    find /app/build/libs -name "*.jar" -exec echo "JAR: {}" \; && \
    echo "" && \
    echo "=== Verificando manifest do app.jar (JAR executável) ===" && \
    if [ -f /app/build/libs/app.jar ]; then \
        echo "app.jar encontrado!" && \
        unzip -p /app/build/libs/app.jar META-INF/MANIFEST.MF | head -20 || echo "Erro ao ler manifest"; \
    else \
        echo "ERRO: app.jar não encontrado!" && \
        echo "Procurando por JARs com padrão -SNAPSHOT.jar:" && \
        find /app/build/libs -name "*-SNAPSHOT.jar" -exec sh -c 'echo "JAR encontrado: $1" && unzip -p "$1" META-INF/MANIFEST.MF | head -10' _ {} \; ; \
    fi

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy all JARs from build stage to a temp location first
COPY --from=build /app/build/libs/*.jar /tmp/libs/

# Select the correct JAR (app.jar from bootJar, or fallback to -SNAPSHOT.jar)
# The bootJar task is configured to generate "app.jar" in build.gradle.kts
RUN echo "=== JARs disponíveis em /tmp/libs ===" && \
    ls -lah /tmp/libs/ && \
    echo "" && \
    if [ -f /tmp/libs/app.jar ]; then \
        echo "Usando app.jar (gerado pelo bootJar)" && \
        cp /tmp/libs/app.jar /app/app.jar; \
    elif [ -n "$(find /tmp/libs -name '*-SNAPSHOT.jar' ! -name '*-plain.jar')" ]; then \
        echo "app.jar não encontrado, usando JAR com padrão -SNAPSHOT.jar" && \
        cp /tmp/libs/*-SNAPSHOT.jar /app/app.jar; \
    else \
        echo "ERRO: Nenhum JAR executável encontrado!" && \
        ls -lah /tmp/libs/ && \
        exit 1; \
    fi && \
    echo "" && \
    echo "=== Verificando JAR final copiado ===" && \
    ls -lah /app/app.jar && \
    echo "" && \
    echo "=== Verificando manifest do JAR (deve conter Main-Class) ===" && \
    unzip -p /app/app.jar META-INF/MANIFEST.MF | head -20 && \
    echo "" && \
    echo "=== Verificando se Main-Class está presente ===" && \
    unzip -p /app/app.jar META-INF/MANIFEST.MF | grep -E "(Main-Class|Spring-Boot)" || echo "AVISO: Main-Class não encontrado no manifest"

# Expose port (Railway will set PORT env var)
EXPOSE 8080

# Run the application
# Railway define PORT automaticamente, a aplicação Spring Boot lê via application.properties
ENTRYPOINT ["java", "-jar", "app.jar"]
