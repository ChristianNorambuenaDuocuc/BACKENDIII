# Proyecto BFF Bancario con JWT, Service Token y HTTPS

Proyecto desarrollado con **Spring Boot 4.1.1** y **Java 21**, utilizando el patrón **Backend for Frontend (BFF)** para separar las necesidades de los clientes Web, Mobile y ATM.

La solución incorpora:

- BFF independiente por tipo de cliente.
- Microservicios de Cuentas, Intereses y Transacciones.
- Autenticación mediante JWT.
- `SERVICE_TOKEN` para la comunicación segura entre BFF y microservicios.
- HTTPS mediante certificado PKCS12 autofirmado.
- Control de acceso por roles.
- Comunicación stateless con Spring Security.

---

## 1. Arquitectura general

```text
                     CLIENTES

          WEB          MOBILE          ATM
           |              |             |
           | JWT_USUARIO  | JWT_USUARIO | JWT_USUARIO
           v              v             v

       BFF WEB       BFF MOBILE      BFF ATM
       :8091         :8092           :8093
           |              |             |
           |        SERVICE_TOKEN        |
           |              |             |
           +--------------+-------------+
                          |
              +-----------+-----------+
              |           |           |
              v           v           v
           CUENTAS     INTERESES   TRANSACCIONES
            :8081        :8082         :8083
```

La seguridad se divide en dos niveles:

```text
Cliente -> BFF
JWT_USUARIO

BFF -> Microservicio
SERVICE_TOKEN
```

El usuario no accede directamente a los microservicios utilizando el JWT obtenido en el login.

---

## 2. Tecnologías utilizadas

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring RestClient
- Spring Security
- JJWT 0.12.6
- Lombok
- Apache Commons CSV
- Maven
- HTTPS / TLS
- PKCS12
- Postman

---

## 3. Módulos del proyecto

```text
semana 4/
|
+-- bffweb/
+-- bffmobile/
+-- bffatm/
|
+-- cuentasservice/
+-- interesesservice/
+-- transaccionesservice/
|
+-- keystore.p12
```

### BFF Web

Puerto:

```text
8091
```

Responsabilidad:

- Autenticar al cliente Web.
- Generar JWT de usuario.
- Validar el JWT recibido.
- Generar `SERVICE_TOKEN`.
- Consultar Cuentas, Intereses y Transacciones.
- Construir una respuesta adaptada al cliente Web.

### BFF Mobile

Puerto:

```text
8092
```

Responsabilidad:

- Autenticar al cliente Mobile.
- Generar JWT de usuario.
- Validar el JWT recibido.
- Generar `SERVICE_TOKEN`.
- Consultar Cuentas e Intereses.
- Construir una respuesta resumida para Mobile.

### BFF ATM

Puerto:

```text
8093
```

Responsabilidad:

- Autenticar al cliente ATM.
- Generar JWT de usuario.
- Validar el JWT recibido.
- Generar `SERVICE_TOKEN`.
- Consultar los microservicios necesarios para las operaciones ATM.

### Microservicio Cuentas

Puerto:

```text
8081
```

Procesa la información asociada a cuentas bancarias.

### Microservicio Intereses

Puerto:

```text
8082
```

Procesa la información de intereses asociados a las cuentas.

### Microservicio Transacciones

Puerto:

```text
8083
```

Procesa la información de transacciones.

---

## 4. Usuarios de prueba

### Web

```text
usuario: webuser
password: web123
rol: WEB_USER
```

### Mobile

```text
usuario: mobileuser
password: mobile123
rol: MOBILE_USER
```

### ATM

```text
usuario: atmuser
password: 1234
rol: ATM_USER
```

---

## 5. Autenticación JWT

Cada BFF tiene un endpoint de login.

### Web

```http
POST https://localhost:8091/auth/login
```

Body:

```json
{
  "username": "webuser",
  "password": "web123"
}
```

### Mobile

```http
POST https://localhost:8092/auth/login
```

Body:

```json
{
  "username": "mobileuser",
  "password": "mobile123"
}
```

### ATM

```http
POST https://localhost:8093/auth/login
```

Body:

```json
{
  "username": "atmuser",
  "password": "1234"
}
```

Respuesta esperada:

```json
{
  "token": "eyJ...",
  "tipo": "Bearer"
}
```

El token obtenido corresponde al:

```text
JWT_USUARIO
```

y solamente se utiliza para acceder al BFF correspondiente.

---

## 6. Flujo del JWT de usuario

Ejemplo Web:

```text
webuser + web123
       |
       v
POST /auth/login
       |
       v
BFF WEB
       |
       v
JWT_USUARIO
       |
       v
Cliente
       |
       | Authorization: Bearer JWT_USUARIO
       v
GET /api/web/cuentas/103/resumen
```

Spring Security valida:

- Firma del token.
- Expiración.
- Usuario.
- Rol.
- Autenticación almacenada en `SecurityContextHolder`.

---

## 7. Service Token

Cuando un BFF necesita llamar a un microservicio, **no reenvía el JWT del usuario**.

El BFF genera un segundo JWT:

```text
SERVICE_TOKEN
```

Ejemplo:

```json
{
  "iss": "bffweb",
  "sub": "webuser",
  "rol": "WEB_USER",
  "email": "webuser@example.com",
  "nombre": "webuser",
  "tipo": "SERVICE_TOKEN",
  "aud": ["semana4"]
}
```

Para Mobile:

```text
iss = bffmobile
```

Para ATM:

```text
iss = bffatm
```

Este token es enviado al microservicio mediante:

```http
Authorization: Bearer SERVICE_TOKEN
```

---

## 8. Flujo completo de seguridad

```text
1. Usuario realiza login
             |
             v
2. BFF valida credenciales
             |
             v
3. BFF genera JWT_USUARIO
             |
             v
4. Cliente llama al BFF
   Authorization: Bearer JWT_USUARIO
             |
             v
5. JwtAuthenticationFilter valida JWT
             |
             v
6. Usuario queda autenticado
   en SecurityContextHolder
             |
             v
7. Client interno del BFF
   genera SERVICE_TOKEN
             |
             v
8. BFF llama al microservicio
   Authorization: Bearer SERVICE_TOKEN
             |
             v
9. ServiceTokenFilter
   valida el token
             |
             v
10. Microservicio procesa petición
```

---

## 9. Clases principales de seguridad

### En los BFF

```text
config/
+-- JwtProperties.java
+-- SecurityConfig.java

security/
+-- JwtTokenUtil.java
+-- JwtAuthenticationFilter.java

controller/
+-- AuthController.java
```

### JwtProperties

Carga desde `application.properties`:

```properties
jwt.secret
jwt.expiration
jwt.service-secret
jwt.service-expiration
```

### JwtTokenUtil

Tiene dos responsabilidades principales:

```java
generateToken(...)
```

Genera el JWT utilizado por:

```text
Cliente -> BFF
```

y:

```java
generateServiceToken(...)
```

Genera el JWT utilizado por:

```text
BFF -> Microservicio
```

### JwtAuthenticationFilter

Intercepta peticiones al BFF y busca:

```http
Authorization: Bearer JWT
```

Valida el token y registra al usuario en:

```text
SecurityContextHolder
```

### SecurityConfig

Define:

- Rutas públicas.
- Rutas protegidas.
- Roles.
- Política `STATELESS`.
- Filtro JWT.

---

## 10. Seguridad en los microservicios

Cada microservicio contiene:

```text
security/
+-- ServiceTokenUtil.java
+-- ServiceTokenFilter.java
```

### ServiceTokenUtil

Valida criptográficamente el `SERVICE_TOKEN`.

Ejemplo:

```java
Jwts.parser()
    .verifyWith(secretKey)
    .build()
    .parseSignedClaims(token)
    .getPayload();
```

### ServiceTokenFilter

Verifica que:

- Exista header `Authorization`.
- Sea tipo `Bearer`.
- La firma sea válida.
- El token no esté expirado.
- El claim `tipo` sea:

```text
SERVICE_TOKEN
```

Si la validación falla:

```text
401 Unauthorized
```

---

## 11. Clave compartida para Service Token

Los BFF utilizan:

```properties
jwt.service-secret=clave-compartida-bff-semana4-2026-1234567890
```

Los microservicios utilizan:

```properties
service-token.secret=clave-compartida-bff-semana4-2026-1234567890
```

Estas claves deben coincidir para que los microservicios puedan validar el token generado por los BFF.

> Para un ambiente productivo, las claves y contraseñas no deben almacenarse directamente en el repositorio. Deben gestionarse mediante variables de entorno, secretos o un gestor de credenciales.

---

## 12. Endpoints principales

### BFF Web

Login:

```http
POST https://localhost:8091/auth/login
```

Resumen:

```http
GET https://localhost:8091/api/web/cuentas/{cuentaId}/resumen
```

Ejemplo:

```http
GET https://localhost:8091/api/web/cuentas/103/resumen
```

### BFF Mobile

Login:

```http
POST https://localhost:8092/auth/login
```

Resumen Mobile:

```http
GET https://localhost:8092/api/mobile/cuentas/{cuentaId}/resumen
```

Ejemplo:

```http
GET https://localhost:8092/api/mobile/cuentas/103/resumen
```

### BFF ATM

Login:

```http
POST https://localhost:8093/auth/login
```

Saldo:

```http
GET https://localhost:8093/api/atm/cuentas/{cuentaId}/saldo
```

Movimientos:

```http
GET https://localhost:8093/api/atm/cuentas/{cuentaId}/movimientos
```

Retiros:

```http
POST https://localhost:8093/api/atm/cuentas/{cuentaId}/retiros
```

---

## 13. Endpoints internos de microservicios

### Cuentas

```http
GET http://localhost:8081/api/cuentas/{cuentaId}
```

### Intereses

```http
GET http://localhost:8082/api/intereses/cuenta/{cuentaId}
```

### Transacciones

```http
GET http://localhost:8083/api/transacciones
```

Estas rutas internas requieren un `SERVICE_TOKEN`.

---

## 14. Pruebas esperadas

### Caso 1 — Acceso correcto al BFF

```text
JWT_USUARIO -> BFF
```

Resultado:

```text
200 OK
```

Ejemplo:

```http
GET https://localhost:8091/api/web/cuentas/103/resumen
Authorization: Bearer JWT_USUARIO
```

Resultado:

```text
200 OK
```

### Caso 2 — Acceso directo al microservicio sin token

```http
GET http://localhost:8081/api/cuentas/103
```

Resultado esperado:

```text
401 Unauthorized
```

### Caso 3 — Acceso al microservicio usando JWT del usuario

```text
JWT_USUARIO -> Microservicio
```

Resultado esperado:

```text
401 Unauthorized
```

Esto ocurre porque el microservicio espera un:

```text
SERVICE_TOKEN
```

y no el JWT generado durante el login.

### Caso 4 — Acceso BFF -> Microservicio

```text
BFF
 |
 | SERVICE_TOKEN
 v
Microservicio
```

Resultado esperado:

```text
200 OK
```

---

## 15. Resumen de pruebas de seguridad

| Petición | Token utilizado | Resultado esperado |
|---|---|---|
| Cliente -> BFF | Sin token | 401/403 |
| Cliente -> BFF | JWT_USUARIO válido | 200 |
| Cliente -> Microservicio | Sin token | 401 |
| Cliente -> Microservicio | JWT_USUARIO | 401 |
| BFF -> Microservicio | SERVICE_TOKEN | 200 |

---

## 16. HTTPS

Los BFF utilizan HTTPS mediante un certificado autofirmado generado con `keytool`.

Comando utilizado:

```powershell
keytool -genkey -alias https_certs -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 365
```

El archivo generado:

```text
keystore.p12
```

se copia dentro de:

```text
bffweb/src/main/resources/
bffmobile/src/main/resources/
bffatm/src/main/resources/
```

Configuración:

```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-type=PKCS12
server.ssl.key-store-password=TU_PASSWORD
server.ssl.key-alias=https_certs
```

Por seguridad, no se incluye la contraseña real en este README.

---

## 17. HTTP vs HTTPS

Antes:

```text
http://localhost:8091
```

Después:

```text
https://localhost:8091
```

BFF disponibles:

```text
https://localhost:8091   BFF Web
https://localhost:8092   BFF Mobile
https://localhost:8093   BFF ATM
```

Los microservicios continúan comunicándose internamente mediante HTTP en este entorno local:

```text
http://localhost:8081
http://localhost:8082
http://localhost:8083
```

---

## 18. Certificado autofirmado y Postman

Al tratarse de un certificado autofirmado, Postman puede rechazar inicialmente la conexión HTTPS.

Para pruebas locales:

```text
Postman
-> Settings
-> General
-> SSL certificate verification
-> OFF
```

Esto se utiliza únicamente para desarrollo local.

---

## 19. Orden recomendado de ejecución

Levantar primero los microservicios:

```text
1. cuentasservice         :8081
2. interesesservice       :8082
3. transaccionesservice   :8083
```

Luego los BFF:

```text
4. bffweb                 :8091
5. bffmobile              :8092
6. bffatm                 :8093
```

---

## 20. Flujo de prueba Web

### Paso 1

Login:

```http
POST https://localhost:8091/auth/login
```

```json
{
  "username": "webuser",
  "password": "web123"
}
```

### Paso 2

Copiar:

```json
{
  "token": "eyJ..."
}
```

### Paso 3

Usar el token en:

```http
GET https://localhost:8091/api/web/cuentas/103/resumen
```

Authorization:

```text
Bearer Token
```

Resultado:

```text
200 OK
```

---

## 21. Flujo de prueba Mobile

Login:

```http
POST https://localhost:8092/auth/login
```

```json
{
  "username": "mobileuser",
  "password": "mobile123"
}
```

Luego:

```http
GET https://localhost:8092/api/mobile/cuentas/103/resumen
```

Resultado esperado:

```text
200 OK
```

---

## 22. Flujo de prueba ATM

Login:

```http
POST https://localhost:8093/auth/login
```

```json
{
  "username": "atmuser",
  "password": "1234"
}
```

Luego, por ejemplo:

```http
GET https://localhost:8093/api/atm/cuentas/103/saldo
```

Resultado esperado:

```text
200 OK
```

---

## 23. Conceptos principales del proyecto

### Backend for Frontend

Cada tipo de cliente tiene un BFF especializado:

```text
Web -> BFF Web
Mobile -> BFF Mobile
ATM -> BFF ATM
```

Esto permite entregar respuestas distintas según las necesidades de cada interfaz.

### JWT

Se utiliza para autenticar al usuario frente al BFF.

```text
Cliente -> JWT_USUARIO -> BFF
```

### Service Token

Se utiliza para proteger la comunicación interna:

```text
BFF -> SERVICE_TOKEN -> Microservicio
```

Además transporta identidad delegada del usuario.

### HTTPS

Protege la información durante el transporte entre el cliente y el BFF:

```text
Cliente -> HTTPS -> BFF
```

---

## 24. Diferencia entre JWT y HTTPS

JWT y HTTPS cumplen funciones diferentes.

```text
JWT
-> autenticación y autorización

HTTPS
-> cifrado de la comunicación

SERVICE_TOKEN
-> autenticación/autorización entre BFF y microservicios
```

El proyecto utiliza los tres mecanismos en conjunto.

---

## 25. Resultado final

La solución final implementa:

```text
                    HTTPS
                      |
CLIENTE ------ JWT_USUARIO ------> BFF
                                   |
                                   |
                             SERVICE_TOKEN
                                   |
                +------------------+------------------+
                |                  |                  |
                v                  v                  v
             CUENTAS           INTERESES        TRANSACCIONES
```

Con esto:

- Los clientes deben autenticarse antes de utilizar un BFF.
- Cada BFF aplica autorización según su rol.
- Los microservicios no aceptan directamente el JWT del usuario.
- Los BFF generan un `SERVICE_TOKEN` para las llamadas internas.
- Los microservicios validan el `SERVICE_TOKEN`.
- Los BFF utilizan HTTPS mediante certificado PKCS12.
- La arquitectura permanece stateless.

---

## Autor

Proyecto académico desarrollado para **Backend III - DUOC UC**.

Implementación:

- Spring Boot
- BFF Pattern
- JWT
- Service Token
- HTTPS
- Microservicios
