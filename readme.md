# Barber Management System - Microservicios Multi-Sucursal

Sistema de gestión para barberías basado en arquitectura de microservicios con Spring Boot, Angular y PostgreSQL, con soporte multi-inquilino (multi-tenant) por base de datos independiente para cada sucursal.

---

## 1. Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado en tu computadora:

- **Docker Desktop** (en Windows o macOS) o **Docker Engine + Docker Compose v2** (en Linux).
- **Git** para clonar el repositorio.
- Puertos disponibles en tu máquina:
  - `4200` (Frontend Angular)
  - `8080` (API Gateway)
  - `8761` (Eureka Service Discovery)
  - `8888` (Spring Cloud Config Server)
  - `5433` (PostgreSQL)
  - `5050` (PgAdmin 4)

---

## 2. Puesta en Marcha Rápida

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/papuproprog/Practica.git
   cd Practica
   ```

2. **Levantar todos los servicios con Docker Compose:**
   ```bash
   docker compose up --build -d
   ```
   *Nota: La primera vez descargará las imágenes y compilará los contenedores. Esto puede tomar entre 3 y 5 minutos.*

3. **Verificar el estado de los contenedores:**
   ```bash
   docker compose ps
   ```
   Espera a que los servicios marquen estado `healthy`.

4. **Abrir la aplicación en el navegador:**
   - **Frontend:** [http://localhost:4200](http://localhost:4200)

---

## 3. Credenciales y Cuentas de Acceso

La base de datos se inicializa automáticamente con datos de prueba (`init.sql`) y cuenta con diferentes niveles de acceso:

### Administrador Global (Superadmin)
Cuenta central para la creación y gestión de sucursales y sus respectivos administradores. No tiene acceso a la información operativa (citas, barberos, clientes) de las sucursales.
- **Usuario:** `superadmin@barber.com`
- **Contraseña:** `superadmin123`
- **Ruta:** `/global-admin`
- **Funcionalidades:**
  - Registrar nuevas sucursales en la plataforma.
  - Crear cuentas de administradores asignadas a sucursales existentes.
  - Ver listado de sucursales y administradores dados de alta.

### Administradores de Sucursales
Cada administrador pertenece a una sucursal y su base de datos está completamente aislada. No pueden ver ni modificar información de otras sucursales.

- **Sucursal Centro:**
  - **Usuario:** `admin@centro.com`
  - **Contraseña:** `centro123`
- **Sucursal Norte:**
  - **Usuario:** `admin@norte.com`
  - **Contraseña:** `norte123`
- **Sucursal Oriente:**
  - **Usuario:** `admin@oriente.com`
  - **Contraseña:** `oriente123`

**Funcionalidades de Administrador de Sucursal:**
- **Agenda:** Visualizar y gestionar citas de la sucursal.
- **Barberos:** Dar de alta y listar barberos de la sucursal.
- **Servicios:** Crear y consultar catálogo de servicios persistidos en base de datos.
- **Nueva Cita:** Agendar citas vinculando cliente, barbero y servicio.

---

## 4. Arquitectura del Sistema

El proyecto está compuesto por los siguientes componentes:

| Servicio | Puerto Local | Descripción |
| :--- | :--- | :--- |
| **Frontend** | `4200` | Aplicación Single Page Application (SPA) construida en Angular. |
| **Gateway** | `8080` | Spring Cloud Gateway con enrutamiento dinámico, validación JWT y propagación del tenant (`X-Branch-Id`). |
| **Eureka Discovery** | `8761` | Registro y descubrimiento de microservicios. |
| **Config Server** | `8888` | Servidor centralizado de configuración de Spring Cloud. |
| **Security Service** | `8084` | Autenticación, emisión y validación de tokens JWT con roles y sucursal. |
| **Barber Service** | `8083` | Gestión de barberos, sucursales y catálogo de servicios. |
| **Client Service** | `8082` | Gestión y registro de clientes. |
| **Date Service** | `8081` | Gestión y programación de citas/fechas. |
| **PostgreSQL** | `5433` (interno 5432) | Base de datos relacional con esquemas independientes por sucursal. |
| **PgAdmin 4** | `5050` | Interfaz web para administración de bases de datos. |

---

## 5. Aislamiento Multi-Sucursal (Multi-Tenancy)

El sistema implementa aislamiento a nivel de base de datos física:
- **Tenant Routing DataSource:** Cada microservicio (`barbers`, `clients`, `dates`, `security`) contiene un enrutador de conexiones dinámico que resuelve la base de datos adecuada según el encabezado HTTP `X-Branch-Id`.
- **Bases de datos por sucursal:**
  - `securitydb_centro`, `securitydb_norte`, `securitydb_sur`, ...
  - `barbersdb_centro`, `barbersdb_norte`, `barbersdb_sur`, ...
  - `clientsdb_centro`, `clientsdb_norte`, `clientsdb_sur`, ...
  - `datesdb_centro`, `datesdb_norte`, `datesdb_sur`, ...
- El **Gateway** extrae automáticamente la sucursal del token JWT y la inyecta de forma transparente en las peticiones hacia los microservicios.

---

## 6. Comandos Útiles

- **Ver logs en tiempo real de todos los servicios:**
  ```bash
  docker compose logs -f
  ```

- **Ver logs de un servicio específico (ejemplo gateway o frontend):**
  ```bash
  docker compose logs -f gateway
  docker compose logs -f frontend
  ```

- **Detener los servicios sin borrar datos:**
  ```bash
  docker compose down
  ```

- **Reiniciar todo desde cero (limpiando volúmenes de base de datos y reconstruyendo):**
  ```bash
  docker compose down -v
  docker compose up --build -d
  ```

- **Ejecutar suite de pruebas automáticas:**
  Si tienes Python 3 con `requests` instalado (`pip install requests`):
  ```bash
  python3 test_suite.py
  ```
  La suite valida el flujo completo de autenticación, aislamiento multi-tenant, CRUD de servicios, barberos, citas y accesos de Superadmin.
