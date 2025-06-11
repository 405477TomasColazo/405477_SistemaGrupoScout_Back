# Sistema Grupo Scout José Hernández - Backend

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Descripción

API REST desarrollada con Spring Boot para la gestión integral del Grupo Scout José Hernández. El sistema maneja la administración de miembros, grupos familiares, eventos, pagos y seguimiento de progresión personal de los scouts.

## 🚀 Características Principales

- **Gestión de Usuarios**: Sistema de autenticación JWT con roles (familias, educadores, administradores)
- **Administración de Miembros**: Gestión de scouts organizados por grupos familiares y secciones
- **Sistema de Eventos**: Creación, gestión y registro de actividades scouts
- **Procesamiento de Pagos**: Integración con MercadoPago para manejo de cuotas y pagos
- **Progresión Personal**: Sistema digital de hojas de marcha para seguimiento de competencias
- **Notificaciones**: Sistema de email automático para comunicaciones
- **Documentación API**: Swagger UI integrado para documentación interactiva

## 🛠️ Stack Tecnológico

- **Framework**: Spring Boot 3.4.4
- **Lenguaje**: Java 21
- **Base de Datos**: MySQL 8.0
- **Seguridad**: Spring Security + JWT (JJWT 0.12.6)
- **ORM**: JPA/Hibernate
- **Documentación**: OpenAPI 3 / Swagger UI
- **Email**: Spring Mail (Gmail SMTP)
- **Pagos**: MercadoPago SDK Java 2.2.0
- **Build**: Maven
- **Otros**: Lombok, MySQL Connector

## 📦 Instalación y Configuración

### Prerrequisitos

- Java 21+
- MySQL 8.0+
- Maven 3.6+

### Configuración de Base de Datos

1. Crear base de datos MySQL:
```sql
CREATE DATABASE jose_hernandez_db;
CREATE USER 'scout_user'@'localhost' IDENTIFIED BY 'scout_password';
GRANT ALL PRIVILEGES ON jose_hernandez_db.* TO 'scout_user'@'localhost';
FLUSH PRIVILEGES;
```

2. Configurar `application.properties`:
```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/jose_hernandez_db
spring.datasource.username=scout_user
spring.datasource.password=scout_password

# JWT
security.jwt.secret-key=tu-clave-secreta-aqui

# Email
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password

# MercadoPago
mercadopago.access-token=tu-access-token
```

### Instalación

1. Clonar el repositorio:
```bash
git clone [URL_DEL_REPOSITORIO]
cd 405477_SistemaGrupoScout_Back/Sistema-Scouts-Jose-Hernandez
```

2. Instalar dependencias y compilar:
```bash
./mvnw clean compile
```

3. Ejecutar la aplicación:
```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## 🎯 Comandos de Desarrollo

```bash
# Compilar el proyecto
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Ejecutar aplicación
./mvnw spring-boot:run

# Crear JAR para producción
./mvnw clean package

# Ejecutar con perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🏗️ Arquitectura

### Estructura de Capas

```
src/main/java/ar/edu/utn/frc/sistemascoutsjosehernandez/
├── controllers/        # Endpoints REST
├── services/          # Lógica de negocio
├── repositories/      # Acceso a datos (JPA)
├── entities/          # Entidades JPA
├── dtos/             # Objetos de transferencia de datos
├── configs/          # Configuraciones (Security, Swagger)
├── security/         # Autenticación JWT
└── util/             # Utilidades
```

### Dominios de Negocio

1. **Identity & Access Management**
   - Autenticación JWT
   - Control de acceso basado en roles
   - Gestión de usuarios y roles

2. **Gestión de Miembros**
   - Estructura familiar: Usuarios → Grupos Familiares → Miembros
   - Organización por secciones (grupos etarios)
   - Tipos de miembro y estados

3. **Sistema de Eventos**
   - Gestión completa de actividades
   - Sistema de registraciones
   - Tipos de evento y estados
   - Adjuntos y documentación

4. **Operaciones Financieras**
   - Procesamiento de pagos con MercadoPago
   - Gestión de cuotas y tarifas
   - Histórico de pagos
   - Múltiples métodos de pago

5. **Progresión Personal**
   - Sistema de competencias por áreas de crecimiento
   - Hojas de marcha digitales
   - Seguimiento de progreso
   - Flujo de aprobación educador-scout

## 📚 API Endpoints

### Principales Endpoints

#### Autenticación
- `POST /auth/login` - Inicio de sesión
- `POST /auth/register` - Registro de usuario

#### Gestión de Miembros
- `GET /api/members` - Listar miembros
- `POST /api/members` - Crear miembro
- `PUT /api/members/{id}` - Actualizar miembro
- `DELETE /api/members/{id}` - Eliminar miembro

#### Eventos
- `GET /api/events` - Listar eventos
- `POST /api/events` - Crear evento
- `PUT /api/events/{id}` - Actualizar evento
- `GET /api/events/{id}/registrations` - Obtener registraciones

#### Pagos
- `GET /api/payments` - Historial de pagos
- `POST /api/payments/process` - Procesar pago
- `POST /api/payments/webhook` - Webhook MercadoPago

#### Progresión Personal
- `GET /api/progression/march-sheet/member/{id}` - Hoja de marcha del miembro
- `POST /api/progression/march-sheet` - Crear hoja de marcha
- `GET /api/competences` - Listar competencias
- `POST /api/competences/{id}/approve` - Aprobar competencia

### Documentación Interactiva

La documentación completa de la API está disponible en:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 🔐 Seguridad

### Autenticación JWT

- Implementación personalizada con JJWT
- Gestión de sesiones sin estado
- Token de acceso con expiración configurable

### Control de Acceso

- **Endpoints Públicos**: `/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/payments/webhook`
- **Autenticación Requerida**: Todos los demás endpoints
- **Roles del Sistema**:
  - `FAMILY`: Gestión de sus propios scouts
  - `EDUCATOR`: Gestión de scouts de sus secciones
  - `ADMIN`: Acceso completo al sistema

### CORS

Configurado para permitir requests desde:
- `http://localhost:4200` (Frontend Angular)

## 🧪 Testing

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar tests específicos
./mvnw test -Dtest=AuthControllerTest

# Generar reporte de cobertura
./mvnw jacoco:report
```

## 📊 Base de Datos

### Modelo de Datos Principal

```sql
-- Gestión de usuarios
users, roles_x_users, family_groups, members

-- Eventos
events, event_registrations, event_attachments

-- Pagos
payments, payment_items, fees

-- Progresión personal
competences, march_sheets, competence_progress, suggested_actions
```

### Configuración

- **Motor**: MySQL 8.0
- **Estrategia DDL**: Hibernate auto-update
- **Dialecto**: MySQL8Dialect
- **Conexión**: HikariCP (pool por defecto)

## 🌐 Integraciones Externas

### MercadoPago
- SDK Java 2.2.0
- Procesamiento de pagos
- Webhooks para notificaciones
- Ambiente de pruebas configurado

### Sistema de Email
- Spring Mail con Gmail SMTP
- Notificaciones automáticas
- Templates personalizables

## 🚀 Despliegue

### Variables de Entorno

```bash
# Base de datos
DB_URL=jdbc:mysql://localhost:3306/jose_hernandez_db
DB_USERNAME=scout_user
DB_PASSWORD=scout_password

# JWT
JWT_SECRET=tu-clave-secreta-segura

# Email
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password

# MercadoPago
MP_ACCESS_TOKEN=tu-access-token
```

### Docker (Opcional)

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/sistema-scouts-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 📝 Contribución

1. Fork el proyecto
2. Crear branch para feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push al branch (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

### Convenciones de Código

- Seguir las convenciones de Java
- Usar Lombok para reducir boilerplate
- Documentar endpoints con OpenAPI
- Tests unitarios para nueva funcionalidad
- Manejo adecuado de excepciones

## 🐛 Reporte de Bugs

Para reportar bugs o solicitar nuevas funcionalidades, crear un issue en el repositorio incluyendo:

- Descripción detallada del problema
- Pasos para reproducir
- Comportamiento esperado vs actual
- Versión del sistema
- Logs relevantes

## 📞 Contacto

Para consultas sobre el proyecto:
- Email: tomas.colazo.federico@gmail.com


---

**Nota**: Este README está actualizado a junio 2025. Para la versión más reciente de la documentación, consultar el repositorio oficial.
