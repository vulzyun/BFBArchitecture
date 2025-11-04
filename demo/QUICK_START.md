# BFB Management - Quick Start Guide

## 🚀 Run the Application

### Option 1: Using Maven (Development)
```powershell
cd C:\Users\QL6479\SchoolDevs\BFBArchitecture\demo
.\mvnw.cmd spring-boot:run
```

### Option 2: Using JAR (Production-like)
```powershell
cd C:\Users\QL6479\SchoolDevs\BFBArchitecture\demo
.\mvnw.cmd clean package -DskipTests
java -jar target\demo-0.0.1-SNAPSHOT.jar
```

## 🔗 Access Points

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/v3/api-docs
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:bfbdb`
  - Username: `sa`
  - Password: (leave empty)

## 🛠️ Useful Commands

### Build without tests
```powershell
.\mvnw.cmd clean package -DskipTests
```

### Build with tests
```powershell
.\mvnw.cmd clean package
```

### Run tests only
```powershell
.\mvnw.cmd test
```

### Kill Java processes (if port 8080 is in use)
```powershell
taskkill /F /IM java.exe
```

### Check what's using port 8080
```powershell
netstat -ano | findstr :8080
```

### Kill specific process by PID
```powershell
taskkill /F /PID <PID_NUMBER>
```

## 📋 API Endpoints Overview

All endpoints are under `/api/contrats`:

- `POST /api/contrats` - Create a new contract
- `GET /api/contrats/{id}` - Get contract by ID
- `GET /api/contrats?clientId=...&vehiculeId=...&etat=...` - Search contracts
- `PATCH /api/contrats/{id}/start` - Start a contract (EN_ATTENTE → EN_COURS)
- `PATCH /api/contrats/{id}/terminate` - Terminate a contract
- `PATCH /api/contrats/{id}/cancel` - Cancel a contract
- `POST /api/contrats/jobs/mark-late` - Mark late contracts job

## 🐛 Troubleshooting

### Port 8080 already in use
```powershell
# Find and kill the process
netstat -ano | findstr :8080
taskkill /F /IM java.exe
```

### Application won't start
```powershell
# Clean rebuild
.\mvnw.cmd clean package -DskipTests
java -jar target\demo-0.0.1-SNAPSHOT.jar
```

### Swagger shows errors
- Make sure SpringDoc OpenAPI version is `2.7.0` (compatible with Spring Boot 3.5.7)
- Check terminal logs for actual error messages
- Verify application started successfully (look for "Started BfbManagementApplication")

## 📦 Key Dependencies

- **Spring Boot**: 3.5.7
- **SpringDoc OpenAPI**: 2.7.0 (⚠️ Important: version 2.3.0 is NOT compatible)
- **Java**: 17
- **H2 Database**: In-memory database for development

## 🏗️ Project Structure

```
demo/
├── src/main/java/com/BFBManagement/
│   ├── architecture/       # Domain layer (Entities, Repos)
│   ├── business/          # Service layer (Business logic)
│   └── presentation/      # Controller layer (REST API, DTOs)
├── src/main/resources/
│   ├── application.yml    # Configuration
│   └── application.properties
└── pom.xml               # Maven dependencies
```

## 📝 Notes

- Database is **in-memory** (H2) - data is lost on restart
- Default profile is used (no active profile)
- Logging level for BFBManagement is DEBUG
- JPA auto-creates tables on startup (ddl-auto: create-drop)
