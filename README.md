# AquaMuestra

## Nombre y propósito
**AquaMuestra** es una aplicación móvil orientada a la industria acuícola que permite registrar muestras de choritos en terreno. Su propósito es solucionar la lentitud y falta de trazabilidad de los registros manuales actuales, permitiendo a los operadores capturar datos estructurados (centro, línea, conteo), asociar evidencia fotográfica y consultar el historial de manera ordenada, incluso en zonas costeras con conectividad limitada.

## Identidad visual
El diseño de la aplicación está inspirado en el entorno marítimo, priorizando la legibilidad en terreno bajo luz natural.

* **Logotipo:** 
  <img width="430" height="334" alt="logo_aquamuestra" src="https://github.com/user-attachments/assets/98f48975-6a4b-443f-844f-baebc7210f63" />

* **Paleta de Colores:**
  * **Principal:** Azul Marino (`#003366`) 
  * **Secundario:** Verde Teal (`#008080`) 
  * **Fondo:** Gris Claro (`#F5F5F5`) 
  * **Texto:** Gris Oscuro (`#212121`) 
  * **Adicional (Alertas):** Naranja (`#FF9800`) 

## Flujo de usuario
A continuación, se representa el flujo principal de navegación mediante un Diagrama de Actividad UML:

<img width="935" height="394" alt="flujo-usuario-uml" src="https://github.com/user-attachments/assets/3aebd5e9-3ebb-439d-8267-56571ba8cf29" />


## Pantallas principales

Los diseños de las interfaces (mockups) se encuentran ubicados en el directorio `docs/diseno/interfaces/`. El MVP consta de las siguientes pantallas:

1. **Login:** Validación de acceso y rol del usuario.
2. **Inicio (Dashboard):** Pantalla principal de distribución con accesos rápidos.
3. **Formulario de Registro:** Interfaz de captura de datos en terreno (centro, línea, conteo) y adjunto de fotografía.
4. **Historial:** Listado de muestras guardadas localmente con opciones de filtro.
5. **Detalle de Muestra:** Vista específica para revisar información ingresada y modificar el estado de validación (rol Supervisor).

## Integrantes

**Equipo Tridente N°18 - Sección 005-V**

* Daniel Villamizar
* Abraham Vivas
* Renato Uribe

## Tecnologías

El proyecto está diseñado para ser implementado con las siguientes tecnologías:

* **Plataforma:** Android
* **Lenguaje:** Kotlin
* **UI Toolkit:** Jetpack Compose
* **Sistema de Diseño:** Material Design 3 (M3)
* **Arquitectura base:** MVVM (Model-View-ViewModel)

## Conexión con el backend

La app se conecta al backend Spring Boot de la carpeta `backend/` (ver `backend/README-backend.md`).

La URL del backend se configura en `local.properties` (raíz del proyecto, **no se sube a git**):

```properties
# Emulador de Android (valor por defecto si no se define): 10.0.2.2 es el localhost del PC
API_BASE_URL=http://10.0.2.2:8080/

# Teléfono físico en la misma red Wi-Fi: usa la IP del PC (ver ipconfig)
# API_BASE_URL=http://192.168.1.50:8080/
```

Para un teléfono físico, agrega también esa IP en `app/src/main/res/xml/network_security_config.xml` (el archivo explica los pasos).

En Android 17 o superior, la primera vez que inicies sesión la app pide el permiso de **dispositivos cercanos / red local**: sin él no puede conectarse a un servidor de la red local.

**Login:** usa los usuarios del backend (`op01`, `su01`, `an01`); las contraseñas están en `backend/.env`. Después del primer ingreso la sesión queda guardada y la app abre aunque no haya señal.
