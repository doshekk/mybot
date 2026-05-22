# Копіюємо файли конфігурації та сирцевий код
# Копіюємо файли конфігурації та сирцевий код
COPY pom.xml .
COPY pom.xml .
COPY src ./src
COPY src ./src


# Компілюємо проект всередині контейнера
# Компілюємо проект всередині контейнера
RUN mvn clean package -DskipTests
RUN mvn clean package -DskipTests


# Крок 2: Легковаговий образ для запуску
# Крок 2: Легковаговий образ для запуску
FROM eclipse-temurin:21-jre-alpine
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
WORKDIR /app


# Копіюємо зібраний JAR-файл з першого кроку
# Копіюємо зібраний JAR-файл з першого кроку
COPY --from=build /app/target/*-jar-with-dependencies.jar app.jar
COPY --from=build /app/target/*-jar-with-dependencies.jar app.jar


# Команда запуску
# Команда запуску
CMD ["java", "-jar", "app.jar"]
CMD ["java", "-jar", "app.jar"]