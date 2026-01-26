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

# Copy the entire build/libs directory from build stage
# This ensures we get all JARs even if the name doesn't match our expectations
COPY --from=build /app/build/libs /tmp/libs

# Debug: Show what was copied
RUN echo "=== Conteúdo de /tmp/libs ===" && \
    ls -lah /tmp/libs/ && \
    echo "" && \
    echo "=== Todos os JARs encontrados ===" && \
    find /tmp/libs -name "*.jar" -exec ls -lah {} \; && \
    echo "" && \
    echo "=== Procurando JAR executável (excluindo -plain.jar) ===" && \
    find /tmp/libs -name "*.jar" ! -name "*-plain.jar" -exec ls -lah {} \;

# Find and copy the executable JAR (exclude -plain.jar which is not executable)
# Priority: 1) app.jar, 2) any -SNAPSHOT.jar (not -plain), 3) any .jar (not -plain)
RUN JAR_FILE="" && \
    if [ -f /tmp/libs/app.jar ]; then \
        JAR_FILE="/tmp/libs/app.jar" && \
        echo "✓ Usando app.jar"; \
    elif [ -n "$(find /tmp/libs -name '*-SNAPSHOT.jar' ! -name '*-plain.jar' 2>/dev/null | head -1)" ]; then \
        JAR_FILE=$(find /tmp/libs -name '*-SNAPSHOT.jar' ! -name '*-plain.jar' | head -1) && \
        echo "✓ Usando JAR com padrão -SNAPSHOT.jar: $JAR_FILE"; \
    elif [ -n "$(find /tmp/libs -name '*.jar' ! -name '*-plain.jar' 2>/dev/null | head -1)" ]; then \
        JAR_FILE=$(find /tmp/libs -name '*.jar' ! -name '*-plain.jar' | head -1) && \
        echo "✓ Usando JAR encontrado: $JAR_FILE"; \
    else \
        echo "✗ ERRO: Nenhum JAR executável encontrado!" && \
        echo "JARs disponíveis em /tmp/libs:" && \
        ls -lah /tmp/libs/ && \
        exit 1; \
    fi && \
    echo "Copiando $JAR_FILE para /app/app.jar" && \
    cp "$JAR_FILE" /app/app.jar && \
    echo "" && \
    echo "=== Verificando JAR final ===" && \
    ls -lah /app/app.jar && \
    echo "" && \
    echo "=== Verificando manifest ===" && \
    unzip -p /app/app.jar META-INF/MANIFEST.MF 2>/dev/null | head -15 && \
    echo "" && \
    echo "=== Verificando Main-Class ===" && \
    (unzip -p /app/app.jar META-INF/MANIFEST.MF 2>/dev/null | grep -q "Main-Class" && echo "✓ Main-Class encontrado") || echo "✗ Main-Class não encontrado!"

# Create non-root user and set permissions
RUN addgroup -S spring && adduser -S spring -G spring && \
    chown spring:spring /app/app.jar && \
    chmod 644 /app/app.jar

USER spring:spring

# Final verification that JAR is accessible
RUN echo "=== Verificação final como usuário spring ===" && \
    ls -lah /app/app.jar && \
    test -f /app/app.jar || (echo "✗ ERRO: app.jar não encontrado!" && exit 1) && \
    test -r /app/app.jar || (echo "✗ ERRO: app.jar não é legível!" && exit 1) && \
    echo "✓ JAR está acessível e pronto para execução"

# Expose port (Railway will set PORT env var)
EXPOSE 8080

# Run the application
# Railway define PORT automaticamente, a aplicação Spring Boot lê via application.properties
ENTRYPOINT ["java", "-jar", "app.jar"]
