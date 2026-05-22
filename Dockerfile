# ЕТАП 1: Збірка проекту
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ЕТАП 2: Фінальний образ для запуску
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S botgroup && adduser -S botuser -G botgroup

# Оновлений рядок, який чітко бере твій зібраний JAR
COPY --from=builder /build/target/AboutPython-1.0-SNAPSHOT.jar app.jar

USER botuser
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]