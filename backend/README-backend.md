# AquaSample API (backend)

API REST en **Spring Boot 4.2 + Java 25 + MySQL 8** para registrar, consultar, revisar y sincronizar las muestras de la app Android AquaSample. Tiene login por roles con **Spring Security (HTTP Basic)**.

## Requisitos
- Java 25 (`java -version`)
- Docker Desktop (para MySQL)
- No hace falta instalar Maven: se usa el wrapper `./mvnw` (o `mvnw.cmd` en Windows).

## 1. Configurar `.env`
```bash
cd backend
cp .env.example .env
```
Edita `.env` y cambia **todas** las claves:
- `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD`, `MYSQL_PORT`: base de datos.
- `DB_URL`, `DB_USER`, `DB_PASSWORD`: conexión del backend (mismo usuario, clave y puerto).
- `DEMO_PASSWORD_OPERARIO`, `DEMO_PASSWORD_SUPERVISOR`, `DEMO_PASSWORD_ANALISTA`: claves de los usuarios demo.

`.env` no se sube a git.

## 2. Levantar MySQL
```bash
docker compose up -d      # crea el contenedor aquasample-mysql con la base "aquasample"
docker compose ps         # debe decir "healthy"
```
Si el puerto 3306 ya está ocupado, cambia `MYSQL_PORT` en `.env` (por ejemplo 3307) y usa el mismo puerto en `DB_URL`.
Los datos quedan en un volumen de Docker. Para borrarlos por completo: `docker compose down -v`.

## 3. Correr el backend
**Git Bash / Linux / macOS:**
```bash
set -a; . ./.env; set +a     # carga las variables de .env
./mvnw spring-boot:run
```

**PowerShell:**
```powershell
Get-Content .env | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object { $k, $v = $_ -split '=', 2; Set-Item "env:$k" $v }
.\mvnw.cmd spring-boot:run
```

Al arrancar por primera vez:
- si no hay usuarios, crea `op01`, `su01` y `an01` con las claves de `.env` (se guardan con BCrypt);
- si no hay muestras, inserta 6 muestras de ejemplo.

## 4. Tests (sin MySQL)
```bash
./mvnw test       # o ./mvnw package
```
Usan el perfil `test` con una base H2 en memoria (`src/test/resources/application-test.properties`).

## Swagger
- Swagger UI: <http://localhost:8080/swagger-ui.html> (público)
- Pulsa **Authorize**, escribe usuario y contraseña, y prueba los endpoints.
- Documento OpenAPI: <http://localhost:8080/v3/api-docs>

## Usuarios y roles
| Usuario | Nombre | Rol | Qué puede hacer |
|---|---|---|---|
| `op01` | Operario Demo | OPERARIO | Ver muestras, crearlas o sincronizarlas en BORRADOR o ENVIADA, subir fotos |
| `su01` | Supervisor Demo | SUPERVISOR | Todo lo anterior en cualquier estado, observar o validar, eliminar, ver reportes |
| `an01` | Analista Demo | ANALISTA | Ver muestras, fotos y reportes (solo lectura) |

Las contraseñas están en tu `.env` (variables `DEMO_PASSWORD_*`).

## Endpoints y permisos
| Método | Ruta | Qué hace | Quién | Respuestas |
|---|---|---|---|---|
| GET | `/api/auth/me` | Usuario autenticado (username, nombre, rol). La app lo usa para el login | Cualquier rol | 200, 401 |
| GET | `/api/muestras` | Lista muestras. Filtros opcionales: `centro`, `tren`, `linea`, `estado`, `desde`, `hasta` (yyyy-MM-dd) | Cualquier rol | 200, 400, 401 |
| GET | `/api/muestras/{id}` | Obtiene una muestra | Cualquier rol | 200, 401, 404 |
| POST | `/api/muestras` | Crea una muestra (el id lo manda la app) | OPERARIO, SUPERVISOR | 201, 400, 401, 403, 409 |
| PUT | `/api/muestras/{id}` | Crea o actualiza (upsert). Lo usa la sincronización: nunca duplica | OPERARIO, SUPERVISOR | 201, 200, 400, 401, 403 |
| PATCH | `/api/muestras/{id}/estado` | Cambia el estado. `{"estado": "...", "comentario": "..."}`; OBSERVADA exige comentario | SUPERVISOR | 200, 400, 401, 403, 404 |
| POST | `/api/muestras/{id}/foto` | Sube la foto (multipart, campo `archivo`, JPEG/PNG, máx. 5 MB) | OPERARIO, SUPERVISOR | 200, 400, 401, 403, 404, 413 |
| GET | `/api/muestras/{id}/foto` | Descarga la foto | Cualquier rol | 200, 401, 404 |
| DELETE | `/api/muestras/{id}` | Elimina una muestra | SUPERVISOR | 204, 401, 403, 404 |
| GET | `/api/reportes/resumen` | Totales, cantidad por estado y por centro, densidad promedio. Filtros `desde`/`hasta` | SUPERVISOR, ANALISTA | 200, 400, 401, 403 |

Regla extra en POST y PUT: un **OPERARIO** solo puede dejar la muestra en `BORRADOR` o `ENVIADA`. Si manda `OBSERVADA` o `VALIDADA` recibe **403**.

Las fotos se guardan en `app.fotos.dir` (por defecto `./uploads`, variable `FOTOS_DIR`), como `{id}.jpg` o `{id}.png`.

## Errores
Siempre en JSON, con el mismo formato (también 401 y 403):
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
├── controller/  MuestraController, AuthController, ReporteController → reciben la petición HTTP
├── dto/         MuestraDto, CambioEstadoDto, UsuarioDto, ResumenDto, Mapper → JSON + validaciones
├── service/     MuestraService, FotoService, ReporteService, UsuarioService → lógica y reglas
├── repository/  MuestraRepository, UsuarioRepository → acceso a MySQL (Spring Data JPA)
├── model/       Muestra, Usuario, EstadoRevision, Rol → tablas "muestras" y "usuarios"
├── security/    SecurityConfig, UsuarioDetailsService, RespuestasSeguridadJson → login y permisos
├── exception/   GlobalExceptionHandler y excepciones → errores 400/403/404/409/413 en JSON
└── config/      OpenApiConfig (Swagger + Authorize), DatosIniciales (usuarios y muestras demo)
```
