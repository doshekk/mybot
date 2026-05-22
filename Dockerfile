# ЕТАП 1: Збірка проекту
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Копіюємо pom.xml та завантажуємо залежності в кеш
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копіюємо код і збираємо JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ЕТАП 2: Фінальний образ для запуску
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Створюємо безпечного користувача
RUN addgroup -S botgroup && adduser -S botuser -G botgroup

# Копіюємо результат збірки
COPY --from=builder /build/target/*-jar-with-dependencies.jar app.jar

USER botuser
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]