# 📊 DIA - Sistema Control de Agua

![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Tailwind](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)

TimeControl es una aplicación web moderna para la gestión y control del tiempo, desarrollada con tecnologías robustas y eficientes.

## ✨ Características Principales

- 🕒 Registro de Usuarios y Controles
- 📊 Dashboard con métricas y visualizaciones
- 👥 Gestión de usuarios y roles
- 🔍 Filtros avanzados para reportes
- 📅 Integración con calendario - hora
- 📱 Diseño responsive con Tailwind CSS
- 🔄 Sincronización en tiempo real

## 🛠 Tecnologías Utilizadas

### Backend
- **Java 17** - Lenguaje principal
- **Spring Boot 3** - Framework backend
- **Spring Security** - Autenticación y autorización
- **Hibernate** - ORM para persistencia de datos
- **Maven** - Gestión de dependencias
- **Tomcat** - Servidor de aplicaciones embebido

### Frontend
- **Thymeleaf** - Motor de plantillas
- **Tailwind CSS** - Framework CSS utility-first
- **JavaScript** - Interactividad
- **Chart.js** - Gráficos y visualizaciones

### Base de Datos
- **MySQL** - Sistema de gestión de base de datos relacional

### Herramientas
- **Lombok** - Reducción de código boilerplate

## 🚀 Instalación

### Requisitos
- Java JDK 17
- Maven 3.8+
- MySQL 8.0+

### Pasos
1. Clonar repositorio:
   ```bash
   git clone https://github.com/LuisErnesto1234/Sistema-DIA-V.2.git
   cd Control-Agua
´´
1. Crear Base de Datos:
   ```bash
   CREATE DATABASE control_agua;

1. Clonar repositorio:
   ```bash
   git clone https://github.com/LuisErnesto1234/Sistema-DIA-V.2.git
   cd Control-Agua
´´
1. Configurar application.properties:

properties
spring.datasource.url=jdbc:mysql://localhost:3306/timecontrol_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña

src/
├── main/
│   ├── java/
│   │   └── com/timecontrol/
│   │       ├── config/        # Configuraciones Spring
│   │       ├── controller/    # Controladores
│   │       ├── model/         # Entidades
│   │       ├── repository/    # Repositorios
│   │       ├── service/       # Servicios
│   │       └── Application.java
│   └── resources/
│       ├── static/            # Assets
│       ├── templates/         # Vistas
│       └── application.properties
