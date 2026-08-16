# Procesamiento Bancario con Spring Batch

Proyecto desarrollado con **Spring Boot y Spring Batch** para procesar información bancaria proveniente de archivos CSV, aplicar reglas de validación y transformación, manejar registros inconsistentes y almacenar los resultados en una base de datos MySQL.

El proyecto implementa tres procesos Batch principales:

1. Reporte de Transacciones Diarias.
2. Cálculo de Intereses Mensuales.
3. Generación de Estados de Cuenta Anuales.

---

## Tecnologías utilizadas

- Java 21
- Spring Boot 4.1.0
- Spring Batch
- Spring JDBC
- MySQL
- Maven
- MySQL Workbench
- Git / GitHub

---

## Objetivo

Procesar datos bancarios provenientes de archivos CSV utilizando Spring Batch.

Cada archivo es leído mediante un `ItemReader`, validado y transformado mediante un `ItemProcessor` y finalmente almacenado en MySQL mediante un `ItemWriter`.

El sistema también permite detectar datos inconsistentes y continuar la ejecución utilizando manejo de excepciones y políticas de `skip`.

---

# Arquitectura del procesamiento

El flujo principal utilizado por los procesos Batch es:

```text
CSV
 ↓
ItemReader
 ↓
DTO
 ↓
ItemProcessor
 ↓
Model
 ↓
ItemWriter
 ↓
MySQL
```

### DTO

Los DTO representan los datos tal como vienen desde los archivos CSV.

Sus atributos se mantienen como `String` para permitir recibir datos incorrectos o con formatos inconsistentes sin provocar un error inmediato durante la lectura.

Ejemplo:

```text
"2024/01/04"
"-200"
"CREDITO"
```

### Model

Los modelos representan los datos una vez procesados y correctamente tipados.

Ejemplo:

```text
String "1000"
        ↓
BigDecimal 1000.00
```

```text
String "2024/01/04"
        ↓
LocalDate 2024-01-04
```

Los objetos DTO y Model existen temporalmente en memoria durante el procesamiento. Los datos se almacenan permanentemente solamente cuando llegan al `ItemWriter`.

---

# Procesos Batch

## 1. Reporte de Transacciones Diarias

Job:

```text
reporteTransaccionesJob
```

Flujo:

```text
transacciones.csv
        ↓
TransaccionesReader
        ↓
TransaccionCsv
        ↓
TransaccionesProcessor
        ↓
Transacciones
        ↓
TransaccionesWriter
        ↓
tabla transacciones
        ↓
resumenTransaccionesStep
        ↓
tabla resumen_transacciones_diarias
```

El proceso valida:

- ID de transacción.
- Formatos de fecha.
- Montos vacíos.
- Montos iguales o menores a cero.
- Tipos de transacción.
- IDs duplicados.

Los tipos válidos son:

```text
credito
debito
```

El segundo Step genera un resumen diario con:

- Fecha.
- Cantidad de transacciones.
- Total de créditos.
- Total de débitos.
- Monto total.

---

## 2. Cálculo de Intereses Mensuales

Job:

```text
calculoInteresesJob
```

Flujo:

```text
intereses.csv
        ↓
InteresesReader
        ↓
InteresCsv
        ↓
InteresesProcessor
        ↓
Intereses
        ↓
InteresesWriter
        ↓
tabla intereses
```

El proceso valida:

- Identificador de cuenta.
- Nombre.
- Edad.
- Saldo.
- Tipo de cuenta.

Los tipos considerados válidos son:

```text
ahorro
prestamo
```

Los registros con tipos no reconocidos son omitidos.

También se descartan cuentas cuyo saldo sea igual o menor a cero.

Actualmente el procesamiento utiliza tasas configuradas en el `InteresesProcessor` para calcular el saldo final.

---

## 3. Estados de Cuenta Anuales

Job:

```text
estadosCuentaAnualesJob
```

Flujo:

```text
cuentas_anuales.csv
        ↓
CuentaAnualReader
        ↓
CuentaAnualCsv
        ↓
CuentaAnualProcessor
        ↓
CuentaAnual
        ↓
CuentaAnualWriter
        ↓
tabla cuentas_anuales
        ↓
resumenCuentaAnualStep
        ↓
tabla resumen_cuentas_anuales
```

El procesamiento permite normalizar diferentes formatos de fechas y validar los movimientos bancarios.

Los movimientos considerados egresos, como:

```text
retiro
compra
pago
```

pueden venir con montos negativos en el archivo original.

Estos valores son normalizados durante el procesamiento para posteriormente calcular correctamente el resumen anual.

Los movimientos con monto `0` son considerados inválidos.

El resumen anual contiene:

- ID de cuenta.
- Cantidad de transacciones.
- Total de depósitos.
- Total de retiros/compras/pagos.
- Saldo anual.

---

# Ejecución completa

Además de los tres Jobs independientes requeridos, existe un Job encargado de ejecutar el procesamiento completo:

```text
procesoCompletoJob
```

Su flujo es:

```text
procesoCompletoJob
        ↓
transaccionesStep
        ↓
resumenTransaccionesStep
        ↓
interesesStep
        ↓
cuentasAnualesStep
        ↓
resumenCuentaAnualStep
```

El Job se configura en:

```properties
spring.batch.job.name=procesoCompletoJob
```

De esta manera los tres procesos se ejecutan automáticamente al iniciar la aplicación.

---

# Manejo de errores

El proyecto utiliza excepciones personalizadas:

```text
TransaccionInvalidaException
InteresInvalidoException
CuentaAnualInvalidaException
```

Los Steps están configurados con tolerancia a errores mediante:

```java
.faultTolerant()
.skip(...)
.skipLimit(20)
```

Cuando un registro presenta información inválida:

```text
Registro
   ↓
Processor
   ↓
Exception
   ↓
SKIP
   ↓
SkipListener
   ↓
Continúa el procesamiento
```

Esto evita que un registro incorrecto detenga completamente el Job.

---

# Listeners

Se implementaron listeners para registrar los elementos descartados durante el procesamiento:

```text
TransaccionesSkipListener
InteresesSkipListener
CuentaAnualSkipListener
```

Ejemplo de salida:

```text
Transacción omitida - id: 3,
fecha: 2024-01-03,
monto: -200,
tipo: debito.
Motivo: El monto debe ser mayor que cero
```

---

# Estructura del proyecto

```text
src/main/java/cl/duoc/semana1
│
├── config
│   ├── BatchConfig.java
│   └── DataSourceConfig.java
│
├── dto
│   ├── TransaccionCsv.java
│   ├── InteresCsv.java
│   └── CuentaAnualCsv.java
│
├── model
│   ├── Transacciones.java
│   ├── Intereses.java
│   └── CuentaAnual.java
│
├── reader
│   ├── TransaccionesReader.java
│   ├── InteresesReader.java
│   └── CuentaAnualReader.java
│
├── processor
│   ├── TransaccionesProcessor.java
│   ├── InteresesProcessor.java
│   └── CuentaAnualProcessor.java
│
├── writer
│   ├── TransaccionesWriter.java
│   ├── InteresesWriter.java
│   └── CuentaAnualWriter.java
│
├── exception
│   ├── TransaccionInvalidaException.java
│   ├── InteresInvalidoException.java
│   └── CuentaAnualInvalidaException.java
│
├── listener
│   ├── TransaccionesSkipListener.java
│   ├── InteresesSkipListener.java
│   └── CuentaAnualSkipListener.java
│
└── tasklet
    ├── ResumenTransaccionesTasklet.java
    └── ResumenCuentaAnualTasklet.java
```

Los recursos se encuentran en:

```text
src/main/resources
│
├── application.properties
├── schema.sql
│
└── data
    ├── transacciones.csv
    ├── intereses.csv
    └── cuentas_anuales.csv
```

---

# Base de datos

La aplicación utiliza MySQL.

La base utilizada durante el desarrollo es:

```text
banco_batch
```

Configuración de ejemplo:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/banco_batch
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.sql.init.mode=always
spring.batch.jdbc.initialize-schema=always

spring.batch.job.name=procesoCompletoJob
```

> Por seguridad, la contraseña real de MySQL no debe almacenarse en un repositorio público.

---

# Tablas principales

El proyecto utiliza las siguientes tablas:

```text
transacciones
intereses
cuentas_anuales
resumen_transacciones_diarias
resumen_cuentas_anuales
```

Spring Batch también crea sus propias tablas de metadata para registrar las ejecuciones de Jobs y Steps.

---

# Inicialización de la base de datos

Las tablas de la aplicación se encuentran definidas en:

```text
src/main/resources/schema.sql
```

La propiedad:

```properties
spring.sql.init.mode=always
```

permite ejecutar automáticamente este script durante el inicio de la aplicación.

Spring Batch inicializa sus propias tablas mediante:

```properties
spring.batch.jdbc.initialize-schema=always
```

---

# Ejecución

Compilar el proyecto:

```bash
mvn clean package
```

Ejecutar:

```bash
mvn spring-boot:run
```

También puede ejecutarse directamente desde el IDE mediante la clase:

```text
Semana1Application
```

Al finalizar correctamente se debería visualizar:

```text
Job: [name=procesoCompletoJob]
status: [COMPLETED]
```

---

# Fuente de datos

Los datos utilizados para el proyecto están basados en:

```text
https://github.com/KariVillagran/bank_legacy_data
```

Los archivos contienen intencionalmente datos inconsistentes para permitir aplicar validaciones, transformaciones y manejo de errores mediante Spring Batch.

---

# Resultado

El proyecto permite:

- Leer archivos CSV mediante Spring Batch.
- Transformar DTO en modelos de dominio.
- Validar datos inconsistentes.
- Normalizar fechas y montos.
- Omitir registros inválidos sin detener el proceso completo.
- Persistir los datos procesados en MySQL.
- Calcular intereses.
- Generar resúmenes diarios de transacciones.
- Generar resúmenes anuales por cuenta.
- Ejecutar todos los procesos mediante un Job completo.