# ЕТАП 1: Збірка проекту у Fat JAR за допомогою Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ЕТАП 2: Легковаговий запуск додатка на JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Створюємо безпечного ізольованого користувача
RUN addgroup -S botgroup && adduser -S botuser -G botgroup

# Копіюємо згенерований плагіном "fat JAR"
COPY --from=builder /build/target/AboutPython-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

# Зміна-маркер для примусового скидання кешу Railway (2026-05-22)
RUN echo "Force cache bust v3"

USER botuser
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport"

# Запуск нашого JAR-файлу
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]