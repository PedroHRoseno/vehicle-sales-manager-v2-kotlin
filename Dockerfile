# Stage 1: Build (testes e docs excluídos via .dockerignore)
FROM gradle:8.5-jdk17-alpine AS build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src

RUN gradle bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs /tmp/libs

RUN set -e && \
    JAR=$(find /tmp/libs -name '*.jar' ! -name '*-plain.jar' 2>/dev/null | head -1) && \
    [ -n "$JAR" ] && cp "$JAR" /app/app.jar || (echo "Erro: Nenhum JAR executável em build/libs" && exit 1)

RUN addgroup -S spring && adduser -S spring -G spring && \
    chown spring:spring /app/app.jar

USER spring:spring

EXPOSE 8080

# PORT injetado pelo Railway; Spring Boot usa server.port=${PORT:8080}
ENTRYPOINT ["java", "-jar", "app.jar"]
