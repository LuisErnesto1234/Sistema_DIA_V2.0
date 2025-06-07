# Usa una imagen con Maven y JDK 17 para la etapa de construcción
FROM maven:3.8.6-eclipse-temurin-17 AS builder

# Directorio de trabajo en el contenedor
WORKDIR /app

# Copia los archivos necesarios para construir (incluyendo mvnw y .mvn/)
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY .mvn .mvn

# Construye el proyecto y genera el JAR
RUN ./mvnw clean package -DskipTests

# Etapa final con solo el JAR
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copia el JAR desde la etapa de construcción
COPY --from=builder /app/target/Control-Agua-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
