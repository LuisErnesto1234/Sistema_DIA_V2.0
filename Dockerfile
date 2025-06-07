# Usa una imagen oficial de OpenJDK 17 como base
FROM eclipse-temurin:17-jdk-alpine

# Directorio de trabajo en el contenedor
WORKDIR /app

# Copia el archivo JAR construido (ajusta el nombre según tu proyecto)
# Copia el JAR específico (evita usar *.jar si hay múltiples archivos)
COPY target/Control-Agua-0.0.1-SNAPSHOT.jar app.jar

RUN ./mvnw -DoutputFile=target/mvn-dependency-list.log -B -DskipTests clean dependency:list install

# Expone el puerto que usa tu aplicación Spring Boot (normalmente 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["sh", "-c", "java -jar target/*.jar"]
