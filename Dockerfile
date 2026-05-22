# =====================================================================
# ЕТАП 1: Збірка проекту (Тут Render сам скачає Maven і збере твій JAR)
# =====================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# КЕШУВАННЯ ЗАЛЕЖНОСТЕЙ: Спочатку копіюємо тільки pom.xml.
# Це дозволить Docker не скачувати всі бібліотеки заново, якщо ти змінив лише код.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копіюємо вихідний код проекту
COPY src ./src

# Збираємо проект у fat-jar (пропускаємо тести, щоб збірка була швидкою)
RUN mvn clean package -DskipTests

# =====================================================================
# ЕТАП 2: Фінальний образ (Тут залишається тільки Java і твій готовий бот)
# =====================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Створюємо безпечного користувача, щоб бот не запускався від імені root (найкраща практика)
RUN addgroup -S botgroup && adduser -S botuser -G botgroup

# Копіюємо зібраний JAR-файл з Етапу 1 (builder) і перейменовуємо в app.jar
COPY --from=builder /build/target/*-jar-with-dependencies.jar app.jar

# Перемикаємось на безпечного користувача
USER botuser

# Оптимальні налаштування пам'яті для Java в Docker-контейнерах
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport"

# Команда для запуску бота
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]