# Крок 1: Беремо легкий образ з Java 21 (або 17, залежно від вашої версії)
FROM eclipse-temurin:21-jre-alpine

# Створюємо робочу папку всередині сервера
WORKDIR /app

# Копіюємо наш зібраний JAR-файл всередину контейнера
# (Перевірте, щоб назва файлу збігалася з тією, що в папці target)
COPY target/*-jar-with-dependencies.jar app.jar

# Команда для запуску бота
CMD ["java", "-jar", "app.jar"]