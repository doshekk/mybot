# ЕТАП 1: Збірка проекту у Fat JAR
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ЕТАП 2: Легковаговий запуск додатка
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Створюємо безпечного користувача
RUN addgroup -S botgroup && adduser -S botuser -G botgroup

# Копіюємо саме згенерований плагіном "fat JAR"
COPY --from=builder /build/target/AboutPython-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

USER botuser
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]