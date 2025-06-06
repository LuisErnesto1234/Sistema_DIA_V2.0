# Usa una imagen oficial de OpenJDK 17 como base
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo en el contenedor
WORKDIR /app

# Copia el archivo JAR construido (ajusta el nombre según tu proyecto)
COPY target/*.jar app.jar

# Expone el puerto que usa tu aplicación Spring Boot (normalmente 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
