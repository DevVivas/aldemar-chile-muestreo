# AquaSample API (backend)

API REST en **Spring Boot 4.2 + Java 25 + MySQL 8** para registrar, consultar y sincronizar las muestras de la app Android AquaSample.

## Requisitos
- Java 25 (`java -version`)
- Docker Desktop (para MySQL)
- No hace falta instalar Maven: se usa el wrapper `./mvnw` (o `mvnw.cmd` en Windows).

## 1. Levantar MySQL
```bash
cd backend
cp .env.example .env      # y cambia las claves dentro de .env
docker compose up -d      # crea el contenedor aquasample-mysql con la base "aquasample"
docker compose ps         # debe decir "healthy"
```
Si el puerto 3306 ya está ocupado, cambia `MYSQL_PORT` en `.env` (por ejemplo 3307) y usa el mismo puerto en `DB_URL`.

Los datos se guardan en un volumen de Docker, así que sobreviven a `docker compose down`.
Para borrarlos por completo: `docker compose down -v`.

## 2. Correr el backend
El backend lee la conexión de las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`.

**Git Bash / Linux / macOS:**
```bash
cd backend
set -a; . ./.env; set +a     # carga las variables de .env
./mvnw spring-boot:run
```

**PowerShell:**
```powershell
cd backend
Get-Content .env | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object { $k, $v = $_ -split '=', 2; Set-Item "env:$k" $v }
.\mvnw.cmd spring-boot:run
```

Al arrancar por primera vez, si la tabla está vacía, se insertan 6 muestras de ejemplo.

## 3. Swagger
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Documento OpenAPI (JSON): <http://localhost:8080/v3/api-docs>

## Endpoints

| Método | Ruta | Qué hace | Respuestas |
|---|---|---|---|
| GET | `/api/muestras` | Lista muestras. Filtros opcionales: `centro`, `tren`, `linea`, `estado`, `desde`, `hasta` (yyyy-MM-dd) | 200, 400 |
| GET | `/api/muestras/{id}` | Obtiene una muestra | 200, 404 |
| POST | `/api/muestras` | Crea una muestra (el id lo manda la app) | 201, 400, 409 si el id ya existe |
| PUT | `/api/muestras/{id}` | Crea o actualiza (upsert). Lo usa la sincronización: nunca duplica | 201 creada, 200 actualizada, 400 |
| DELETE | `/api/muestras/{id}` | Elimina una muestra | 204, 404 |

Los errores siempre vienen en JSON:
```json
{
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Hay campos con errores",
  "ruta": "/api/muestras",
  "errores": { "centro": "El centro es obligatorio", "tramoCm": "El tramo debe ser mayor a 0 cm" }
}
```

## Estructura
```
src/main/java/com/aldemar/aquasample/
├── controller/  MuestraController      → recibe la petición HTTP
├── dto/         MuestraDto, Mapper     → JSON de entrada/salida + validaciones
├── service/     MuestraService         → lógica (crear, upsert, 404, 409)
├── repository/  MuestraRepository      → acceso a MySQL (Spring Data JPA)
├── model/       Muestra, EstadoRevision→ tabla "muestras"
├── exception/   GlobalExceptionHandler → errores 400/404/409 en JSON
└── config/      OpenApiConfig, DatosIniciales
```
