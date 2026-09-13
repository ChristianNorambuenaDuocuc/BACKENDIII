Proyecto Sistema Bancario con Backend for Frontend (BFF)

## Descripción

Este proyecto implementa una arquitectura basada en **microservicios** y el patrón **Backend for Frontend (BFF)** para un sistema bancario.

La solución separa los servicios de negocio de los backends específicos para cada tipo de cliente:

- **Web**
- **Móvil**
- **Cajero Automático (ATM)**

Cada canal posee su propio BFF, permitiendo adaptar la información, las operaciones y la seguridad a las necesidades de cada cliente.

---

## Arquitectura general

```text
                         ┌─────────────────────┐
WEB ────────────────────►│       BFF Web       │ :8091
                         └─────────┬───────────┘
                                   │
                         ┌─────────▼───────────┐
MÓVIL ──────────────────►│      BFF Mobile     │ :8092
                         └─────────┬───────────┘
                                   │
                         ┌─────────▼───────────┐
ATM ────────────────────►│       BFF ATM       │ :8093
                         └─────────┬───────────┘
                                   │
                   ┌───────────────┼────────────────┐
                   │               │                │
                   ▼               ▼                ▼
          cuentas-service   intereses-service   transacciones-service
               :8081              :8082                :8083
```

Los BFF no acceden directamente a los archivos CSV.  
Cada BFF consume las APIs REST expuestas por los microservicios de negocio.

---

# 1. Microservicios de negocio

## 1.1 cuentas-service

**Puerto:** `8081`

Responsable de exponer la información contenida en `cuentas_anuales.csv`.

### Datos principales

- `cuenta_id`
- `fecha`
- `transaccion`
- `monto`
- `descripcion`

### Endpoints

Obtener todos los registros:

```http
GET http://localhost:8081/api/cuentas
```

Obtener movimientos asociados a una cuenta:

```http
GET http://localhost:8081/api/cuentas/{cuentaId}
```

Ejemplo:

```http
GET http://localhost:8081/api/cuentas/103
```

### Estructura interna

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
CSV
```

Además utiliza:

```text
Model
 ↓
Mapper
 ↓
DTO
```

---

## 1.2 intereses-service

**Puerto:** `8082`

Responsable de exponer la información contenida en `intereses.csv`.

### Datos principales

- `intereses_id`
- `nombre`
- `saldo`
- `edad`
- `tipo`

### Endpoints

Obtener todos los registros:

```http
GET http://localhost:8082/api/intereses
```

Obtener un registro según el identificador disponible en el archivo:

```http
GET http://localhost:8082/api/intereses/{interesesId}
```

### Nota importante

El archivo utiliza:

```text
intereses_id
```

y no:

```text
cuenta_id
```

Por lo tanto, **no se debe asumir que `intereses_id` corresponde al mismo identificador que `cuenta_id`**. Si se combinan datos entre servicios dentro de un BFF, esta diferencia debe considerarse para no crear una relación inexistente entre los archivos originales.

---

## 1.3 transacciones-service

**Puerto:** `8083`

Responsable de exponer la información contenida en `transacciones.csv`.

### Datos principales

- `id`
- `fecha`
- `monto`
- `tipo`

### Endpoints

Obtener todas las transacciones:

```http
GET http://localhost:8083/api/transacciones
```

Obtener una transacción por ID:

```http
GET http://localhost:8083/api/transacciones/{id}
```

Ejemplo:

```http
GET http://localhost:8083/api/transacciones/1
```

### Nota importante

`transacciones.csv` no contiene un campo `cuenta_id`, por lo que no debe inventarse una asociación directa entre una transacción y una cuenta específica.

---

# 2. Backend for Frontend

La estrategia seleccionada para este proyecto es:

> **BFF por cliente o canal.**

Cada cliente cuenta con un backend independiente optimizado para sus necesidades.

---

## 2.1 BFF Web

**Proyecto:** `bff-web`  
**Puerto:** `8091`

El BFF Web entrega información más completa, adecuada para una interfaz de navegador con mayor capacidad de visualización.

### Endpoint principal

```http
GET http://localhost:8091/api/web/cuentas/103/resumen
```

### Flujo

```text
Navegador
    ↓
BFF Web
    ├──► cuentas-service
    ├──► intereses-service
    └──► transacciones-service
```

El BFF combina las respuestas de los servicios y construye un DTO adaptado a la interfaz Web.

### Seguridad

```text
Usuario: webuser
Contraseña: web123
Rol: WEB_USER
```

Rutas protegidas:

```text
/api/web/**
```

---

## 2.2 BFF Mobile

**Proyecto:** `bff-mobile`  
**Puerto:** `8092`

Está diseñado para entregar respuestas más pequeñas y reducir el consumo de datos.

### Endpoint principal

```http
GET http://localhost:8092/api/mobile/cuentas/103/resumen
```

### Características

- Menor cantidad de campos.
- Respuestas más ligeras.
- Cantidad reducida de movimientos.
- Información adaptada a dispositivos móviles.

### Flujo

```text
Aplicación móvil
      ↓
BFF Mobile
    ├──► cuentas-service
    └──► intereses-service
```

### Seguridad

```text
Usuario: mobileuser
Contraseña: mobile123
Rol: MOBILE_USER
```

Rutas protegidas:

```text
/api/mobile/**
```

---

## 2.3 BFF ATM

**Proyecto:** `bff-atm`  
**Puerto:** `8093`

Está orientado a operaciones específicas de cajeros automáticos.

### Consulta de saldo

```http
GET http://localhost:8093/api/atm/cuentas/103/saldo
```

### Consulta de movimientos

```http
GET http://localhost:8093/api/atm/cuentas/103/movimientos
```

### Validación de retiro

```http
POST http://localhost:8093/api/atm/cuentas/103/retiros
```

Body de ejemplo:

```json
{
  "monto": 1000
}
```

Respuesta esperada:

```json
{
  "cuentaId": 103,
  "montoSolicitado": 1000,
  "aprobado": true,
  "mensaje": "Retiro autorizado"
}
```

### Importante

La operación de retiro implementada en el BFF **valida** si existe saldo suficiente, pero no persiste un nuevo saldo en el CSV.

El objetivo es demostrar la adaptación de operaciones mediante BFF, no implementar un core bancario transaccional completo.

### Seguridad

```text
Usuario: atmuser
Contraseña: 1234
Rol: ATM_USER
```

Rutas protegidas:

```text
/api/atm/**
```

---

# 3. Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- RestClient
- Apache Commons CSV
- Maven
- APIs REST
- JSON

---

# 4. DTO y Mapper

Los microservicios utilizan DTO y Mapper para separar el modelo interno de los datos expuestos por la API.

Ejemplo:

```text
CSV
 ↓
Repository
 ↓
Model
 ↓
Service
 ↓
Mapper
 ↓
DTO
 ↓
Controller
 ↓
JSON
```

Los BFF también utilizan DTO específicos para cada cliente.

Ejemplo:

```text
DTO de cuentas ───────────┐
                          │
DTO de intereses ─────────┼──► Mapper BFF ──► DTO Web / Mobile / ATM
                          │
DTO de transacciones ─────┘
```

De esta forma, cada canal recibe únicamente la información que necesita.

---

# 5. Configuración de puertos

| Aplicación | Puerto |
|---|---:|
| cuentas-service | 8081 |
| intereses-service | 8082 |
| transacciones-service | 8083 |
| bff-web | 8091 |
| bff-mobile | 8092 |
| bff-atm | 8093 |

Cada aplicación debe utilizar un puerto distinto para poder ejecutarse simultáneamente.

---

# 6. Orden recomendado de ejecución

Primero iniciar los microservicios de negocio:

```text
1. cuentas-service
2. intereses-service
3. transacciones-service
```

Después iniciar los BFF:

```text
4. bff-web
5. bff-mobile
6. bff-atm
```

Antes de probar un BFF, los servicios que consume deben encontrarse ejecutándose.

---

# 7. Pruebas básicas

## cuentas-service

```http
GET http://localhost:8081/api/cuentas
GET http://localhost:8081/api/cuentas/103
```

## intereses-service

```http
GET http://localhost:8082/api/intereses
```

## transacciones-service

```http
GET http://localhost:8083/api/transacciones
GET http://localhost:8083/api/transacciones/1
```

## BFF Web

```http
GET http://localhost:8091/api/web/cuentas/103/resumen
```

Credenciales:

```text
webuser / web123
```

## BFF Mobile

```http
GET http://localhost:8092/api/mobile/cuentas/103/resumen
```

Credenciales:

```text
mobileuser / mobile123
```

## BFF ATM

```http
GET http://localhost:8093/api/atm/cuentas/103/saldo
GET http://localhost:8093/api/atm/cuentas/103/movimientos
```

Credenciales:

```text
atmuser / 1234
```

---

# 8. Prueba de retiro ATM con Postman

Método:

```text
POST
```

URL:

```text
http://localhost:8093/api/atm/cuentas/103/retiros
```

Seleccionar:

```text
Body → raw → JSON
```

Ejemplo válido:

```json
{
  "monto": 1000
}
```

Ejemplo para probar saldo insuficiente:

```json
{
  "monto": 99999999
}
```

Ejemplo para probar monto inválido:

```json
{
  "monto": -1000
}
```

---

# 9. Autenticación y autorización por canal

La actividad exige gestionar autenticación y autorización específica para cada cliente.

Se implementó Spring Security con HTTP Basic y roles independientes.

| Canal | Usuario | Contraseña | Rol |
|---|---|---|---|
| Web | `webuser` | `web123` | `WEB_USER` |
| Mobile | `webuser` | `web123` | `WEB_USER` |
| ATM | `webuser` | `web123` | `WEB_USER` |

Esta configuración permite demostrar que cada BFF posee reglas de acceso independientes.

> Las credenciales están configuradas únicamente con fines académicos y de prueba. En un sistema real no deberían almacenarse directamente en el código fuente.

---

# 10. Dependencias principales

## Microservicios que leen CSV

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.14.1</version>
</dependency>
```

## BFF

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-restclient</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

# 11. Estructura general sugerida

```text
proyecto-bancario-bff/
│
├── cuentas-service/
│   ├── controller/
│   ├── dto/
│   ├── mapper/
│   ├── model/
│   ├── repository/
│   └── service/
│
├── intereses-service/
│   ├── controller/
│   ├── dto/
│   ├── mapper/
│   ├── model/
│   ├── repository/
│   └── service/
│
├── transacciones-service/
│   ├── controller/
│   ├── dto/
│   ├── mapper/
│   ├── model/
│   ├── repository/
│   └── service/
│
├── bff-web/
│   ├── client/
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── mapper/
│   └── service/
│
├── bff-mobile/
│   ├── client/
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── mapper/
│   └── service/
│
└── bff-atm/
    ├── client/
    ├── config/
    ├── controller/
    ├── dto/
    ├── mapper/
    └── service/
```

---

# 12. Estrategia BFF seleccionada

La estrategia utilizada es **BFF por tipo de cliente o canal**.

Se eligió porque Web, Mobile y ATM poseen necesidades diferentes:

### Web

Requiere respuestas completas y soporte para interfaces con mayor cantidad de información.

### Mobile

Necesita respuestas ligeras para optimizar velocidad y consumo de ancho de banda.

### ATM

Necesita endpoints reducidos y orientados a operaciones críticas como consulta de saldo, movimientos y retiro.

La separación evita que todos los clientes dependan de una API genérica que entregue información innecesaria.

---

# 13. Conclusión

La solución demuestra la implementación del patrón **Backend for Frontend** mediante tres BFF independientes que consumen microservicios de negocio comunes.

La arquitectura permite:

- Separar responsabilidades.
- Adaptar respuestas según cada cliente.
- Mantener los servicios de negocio independientes de las interfaces.
- Implementar seguridad específica por canal.
- Reducir datos innecesarios en Mobile y ATM.
- Mantener una solución extensible para futuros servicios o clientes.

La implementación corresponde a un ejercicio académico y simplifica aspectos que en un sistema bancario real requerirían mecanismos adicionales de persistencia, trazabilidad, cifrado, gestión de identidad, tolerancia a fallos, auditoría y seguridad transaccional.