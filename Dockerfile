# Dockerfile commun aux services Java VolApp.
# Build : docker build --build-arg MODULE=recherche-service -t volapp-recherche .

# Etape 1 : compilation Maven
FROM maven:3.9-eclipse-temurin-25 AS build
ARG MODULE
WORKDIR /build
COPY . .
RUN mvn -q -B -pl ${MODULE} -am package -DskipTests

# Etape 2 : image d'execution
FROM eclipse-temurin:25-jre
ARG MODULE
WORKDIR /app
COPY --from=build /build/${MODULE}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
