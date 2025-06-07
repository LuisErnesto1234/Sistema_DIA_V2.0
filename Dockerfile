# Etapa de construcción con Maven y JDK 17
FROM maven:3.8.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia los archivos necesarios (incluyendo mvnw y .mvn/)
COPY . .

# Asegúrate de que mvnw sea ejecutable (esto soluciona el error)
RUN chmod +x mvnw

# Construye el proyecto
RUN ./mvnw clean package -DskipTests

# Etapa final con solo el JAR
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/Control-Agua-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
