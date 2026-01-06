# Étape 1 : Build
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# On copie le pom.xml et on télécharge les dépendances (cache Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# On copie le code source et on build (on saute les tests ici car 
# le pipeline CI/CD les aura déjà lancés avant pour valider l'étape)
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]