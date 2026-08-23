
# Procesamiento Bancario con Spring Batch

Proyecto desarrollado con **Spring Boot y Spring Batch** para procesar información bancaria proveniente de archivos CSV, aplicar reglas de validación y transformación, manejar registros inconsistentes y almacenar los resultados en una base de datos MySQL.

El proyecto implementa tres procesos Batch principales:

1. Reporte de Transacciones Diarias.
2. Cálculo de Intereses Mensuales.
3. Generación de Estados de Cuenta Anuales.

Además, incorpora tolerancia a fallos, políticas de reintento, trazabilidad mediante listeners y procesamiento paralelo utilizando múltiples hilos.

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

# Objetivo

Procesar datos bancarios provenientes de archivos CSV utilizando Spring Batch.

Cada archivo es leído mediante un `ItemReader`, representado inicialmente mediante objetos DTO, validado y transformado mediante un `ItemProcessor` y finalmente almacenado en MySQL mediante un `ItemWriter`.

El sistema está diseñado considerando que los datos provenientes de sistemas anteriores pueden contener información incorrecta, mal clasificada o estructuralmente inconsistente.

Para evitar que un registro defectuoso detenga completamente un proceso Batch, se implementaron:

- Validaciones.
- Excepciones personalizadas.
- Políticas de `skip`.
- Políticas de `retry`.
- `SkipListener`.
- `StepExecutionListener`.
- Procesamiento concurrente mediante tres hilos.

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

## DTO

Los DTO representan los datos tal como vienen desde los archivos CSV.

Sus atributos se mantienen principalmente como `String` para permitir recibir información con formatos incorrectos sin provocar un error inmediato durante la lectura.

Ejemplo:

```text
"2024/01/04"
"-200"
"CREDITO"
```

Posteriormente, el `ItemProcessor` valida y transforma estos valores.

---

## Model

Los modelos representan los datos una vez validados, normalizados y correctamente tipados.

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

Los DTO y modelos existen temporalmente en memoria durante el procesamiento.

Los datos se almacenan permanentemente cuando llegan al `ItemWriter` y son persistidos en MySQL.

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
- Consistencia de los datos antes de persistirlos.

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

Los tipos válidos son:

```text
ahorro
prestamo
```

Los registros con tipos no reconocidos son omitidos.

También son rechazadas las cuentas cuyo saldo sea igual o menor a cero.

---

# Tasas de interés configurables

Las tasas utilizadas para calcular los intereses no se encuentran definidas directamente dentro del código Java.

Se encuentran externalizadas en:

```text
src/main/resources/application.properties
```

Actualmente se utilizan:

```properties
intereses.tasa.ahorro=0.01
intereses.tasa.prestamo=0.02
```

Donde:

```text
0.01 = 1%
0.02 = 2%
```

El `InteresesProcessor` obtiene estos valores desde la configuración de Spring.

Esto permite modificar las tasas de interés sin alterar la lógica del código fuente.

Por ejemplo, si la tasa de ahorro cambia a un 1,5%, únicamente se requiere modificar:

```properties
intereses.tasa.ahorro=0.015
```

Los nuevos valores serán utilizados en las ejecuciones posteriores del proceso Batch.

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

pueden venir con montos negativos desde el archivo original.

Estos valores son normalizados durante el procesamiento para posteriormente calcular correctamente los resultados anuales.

Los movimientos cuyo monto sea `0` son considerados inválidos.

El resumen anual contiene:

- ID de cuenta.
- Cantidad de transacciones.
- Total de depósitos.
- Total de retiros, compras y pagos.
- Saldo anual.

---

# Ejecución completa

Además de los tres Jobs independientes, existe un Job encargado de ejecutar el procesamiento completo:

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

El Job se configura mediante:

```properties
spring.batch.job.name=procesoCompletoJob
```

De esta manera, los procesos se ejecutan automáticamente al iniciar la aplicación.

---

# Manejo de errores y excepciones

El procesamiento fue diseñado considerando que los archivos provenientes de sistemas anteriores pueden contener información incorrecta, mal clasificada o estructuralmente inconsistente.

Se utilizan excepciones personalizadas para representar errores asociados a reglas de negocio:

```text
TransaccionInvalidaException
InteresInvalidoException
CuentaAnualInvalidaException
```

También se controlan excepciones técnicas de Spring:

```text
FlatFileParseException
DuplicateKeyException
CannotAcquireLockException
TransientDataAccessException
```

Cada una cumple una función diferente.

### `FlatFileParseException`

Permite manejar problemas producidos durante la lectura o interpretación de registros provenientes de archivos CSV.

### `DuplicateKeyException`

Permite manejar colisiones de claves primarias durante la persistencia en la base de datos.

### `CannotAcquireLockException`

Representa situaciones temporales en las cuales no es posible adquirir un bloqueo requerido en la base de datos.

### `TransientDataAccessException`

Representa errores temporales de acceso a datos que podrían resolverse al intentar nuevamente la operación.

---

# Política de tolerancia a fallos

Los Steps principales utilizan:

```java
.faultTolerant()
```

para aplicar políticas de tolerancia a errores.

La estrategia distingue entre:

```text
Error de datos
      ↓
SKIP
```

y:

```text
Error temporal de infraestructura
      ↓
RETRY
```

---

## Política de Skip

Cuando un registro contiene información inválida, el procesamiento puede omitirlo y continuar con los demás registros.

Se utilizan, según el Step:

```java
.skip(TransaccionInvalidaException.class)
.skip(InteresInvalidoException.class)
.skip(CuentaAnualInvalidaException.class)
.skip(FlatFileParseException.class)
.skip(DuplicateKeyException.class)
```

El límite configurado es:

```java
.skipLimit(20)
```

Por lo tanto, cada Step puede tolerar hasta 20 omisiones.

Si se supera ese límite, el Step finaliza con error.

El flujo es:

```text
Registro
   ↓
Reader / Processor / Writer
   ↓
Excepción controlada
   ↓
SKIP
   ↓
SkipListener
   ↓
Continúa el procesamiento
```

---

## Política de Retry

Los errores temporales de acceso a la base de datos no necesariamente representan información inválida.

Por este motivo se configuraron reintentos mediante:

```java
.retryLimit(3)
.retry(CannotAcquireLockException.class)
.retry(TransientDataAccessException.class)
```

Esto permite realizar hasta tres intentos cuando se presentan problemas temporales.

El flujo es:

```text
Operación
   ↓
Error temporal
   ↓
RETRY
   ↓
Nuevo intento
```

Si el problema continúa después del número máximo de intentos, Spring Batch considera fallida la operación.

---

# Criterio general de tolerancia a fallos

La política utilizada sigue el siguiente criterio:

```text
Dato incorrecto o inconsistente
→ SKIP

Error temporal de base de datos
→ RETRY

Error no contemplado
→ ERROR DEL STEP

Más de 20 registros omitidos
→ ERROR DEL STEP
```

De esta forma, el sistema mantiene continuidad operativa sin ignorar errores críticos.

---

# Tratamiento de registros duplicados

La consistencia de los identificadores se encuentra protegida durante la persistencia en la base de datos.

Las tablas que requieren identificadores únicos utilizan claves primarias.

Por ejemplo:

```sql
id BIGINT PRIMARY KEY
```

Si durante la escritura se intenta insertar un registro cuya clave primaria ya existe, MySQL genera una excepción de duplicidad.

Spring traduce esta situación mediante:

```text
DuplicateKeyException
```

Los Steps correspondientes se encuentran configurados para omitir esta excepción mediante la política de `skip`.

De esta manera:

```text
Registro duplicado
       ↓
ItemWriter
       ↓
MySQL detecta clave repetida
       ↓
DuplicateKeyException
       ↓
SKIP
       ↓
SkipListener
       ↓
Continúa el procesamiento
```

Esta estrategia permite:

- Evitar sobrescrituras accidentales.
- Mantener la integridad de las claves primarias.
- Conservar la información previamente persistida.
- Evitar que una colisión individual interrumpa completamente el proceso.
- Mantener trazabilidad mediante los logs.

---

# Listeners y trazabilidad

El proyecto utiliza listeners para registrar información relevante sobre las ejecuciones.

## SkipListener

Se implementaron:

```text
TransaccionesSkipListener
InteresesSkipListener
CuentaAnualSkipListener
```

Estos listeners registran elementos descartados durante:

- Lectura.
- Procesamiento.
- Escritura.

Ejemplo:

```text
Transacción omitida
id: 3
fecha: 2024-01-03
monto: -200
tipo: debito

Motivo:
El monto debe ser mayor que cero
```

---

## BatchStepExecutionListener

También se implementó:

```text
BatchStepExecutionListener
```

Este listener permite registrar información general de cada Step.

Al iniciar:

```text
[Thread: main] Iniciando step: transaccionesStep
```

Al finalizar:

```text
Finalizó step: transaccionesStep
leídos: 10
escritos: 8
omitidos: 2
estado: COMPLETED
```

Esto permite mantener trazabilidad sobre el comportamiento general de cada etapa.

---

# Política de escalamiento

Los Steps principales fueron configurados para utilizar procesamiento paralelo mediante un:

```text
ThreadPoolTaskExecutor
```

La configuración utilizada es:

```java
executor.setCorePoolSize(3);
executor.setMaxPoolSize(3);
executor.setQueueCapacity(25);
executor.setThreadNamePrefix("Batch-");
```

Esto define un máximo de:

```text
3 hilos de ejecución paralela
```

Los hilos utilizados son identificados como:

```text
Batch-1
Batch-2
Batch-3
```

---

## Tamaño de Chunk

Cada Step principal utiliza un tamaño de chunk de:

```text
5 registros
```

Por ejemplo:

```java
.<TransaccionCsv, Transacciones>chunk(5)
```

El procesamiento se realiza por grupos de cinco elementos.

La combinación utilizada es:

```text
Chunk size = 5
Threads = 3
```

---

# Procesamiento paralelo

El `ThreadPoolTaskExecutor` es asignado a los Steps mediante:

```java
.taskExecutor(taskExecutor)
```

Esto permite que diferentes registros sean procesados simultáneamente por diferentes hilos.

Durante las pruebas se pudo observar:

```text
[Thread: Batch-1] Procesando transacción ID: 2
[Thread: Batch-2] Procesando transacción ID: 1
[Thread: Batch-3] Procesando transacción ID: 3
```

También se comprobó procesamiento concurrente para:

- Transacciones.
- Intereses.
- Estados de cuenta anuales.

La estrategia permite mejorar la capacidad de procesamiento frente a volúmenes mayores de información.

---

# Consideración sobre procesamiento concurrente

Los lectores utilizados para los archivos CSV implementan `ItemStream`.

Spring Batch advierte que la utilización de procesamiento asíncrono junto con lectores que mantienen estado puede afectar la información utilizada para reiniciar una ejecución interrumpida.

Durante la ejecución puede observarse una advertencia similar a:

```text
ItemStream was opened in a different thread.
Restart data could be compromised.
```

Esta advertencia no impide el procesamiento paralelo requerido para la actividad, pero debe ser considerada en un escenario productivo donde sea necesario garantizar recuperación exacta desde un punto de interrupción.

---

# Cierre del ThreadPoolTaskExecutor

Se implementó la clase:

```text
ExecutorShutdown
```

Su responsabilidad es cerrar correctamente el pool de hilos utilizado durante el procesamiento.

La clase utiliza:

```java
@PreDestroy
```

para ejecutar el cierre cuando finaliza la aplicación.

El proceso puede observarse en los logs:

```text
Cerrando ThreadPoolTaskExecutor.
ThreadPoolTaskExecutor cerrado correctamente.
```

De esta forma se evita mantener recursos activos después de completar la ejecución de los Jobs.

---

# Estructura del proyecto

```text
src/main/java/cl/duoc/semana2
│
├── config
│   ├── BatchConfig.java
│   ├── DataSourceConfig.java
│   └── ExecutorShutdown.java
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
│   ├── BatchStepExecutionListener.java
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

Ejemplo de configuración:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/banco_batch
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.sql.init.mode=always
spring.batch.jdbc.initialize-schema=always
spring.batch.job.name=procesoCompletoJob
```

> Por seguridad, la contraseña real de MySQL no debe almacenarse en repositorios públicos.

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

Spring Batch también utiliza sus propias tablas de metadata para registrar:

- Jobs.
- Steps.
- Ejecuciones.
- Parámetros.
- Estados de ejecución.

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

Spring Batch inicializa sus tablas internas mediante:

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

También puede ejecutarse directamente desde el IDE mediante la clase principal:

```text
semana2Application
```

Cuando la ejecución completa finaliza correctamente se obtiene:

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

Los archivos contienen intencionalmente datos inconsistentes para permitir demostrar:

- Validaciones.
- Transformaciones.
- Manejo de excepciones.
- Políticas de tolerancia a fallos.
- Omisión de registros inválidos.
- Persistencia de datos válidos.
- Procesamiento paralelo.

---

# Resultado

El proyecto permite:

- Leer archivos CSV mediante Spring Batch.
- Representar los datos de entrada mediante DTO.
- Transformar DTO en modelos de dominio.
- Validar información inconsistente.
- Normalizar fechas.
- Normalizar montos.
- Validar tipos de transacción y tipos de cuenta.
- Externalizar tasas de interés mediante `application.properties`.
- Omitir registros inválidos sin detener el procesamiento completo.
- Controlar errores estructurales de archivos CSV.
- Controlar colisiones de claves primarias.
- Aplicar políticas de `skip`.
- Aplicar políticas de `retry`.
- Mantener trazabilidad mediante listeners.
- Registrar estadísticas de ejecución de cada Step.
- Persistir información procesada en MySQL.
- Calcular intereses.
- Generar resúmenes diarios de transacciones.
- Generar resúmenes anuales por cuenta.
- Ejecutar los procesos con chunks de tamaño 5.
- Utilizar tres hilos de procesamiento paralelo.
- Cerrar correctamente el `ThreadPoolTaskExecutor`.
- Ejecutar todo el flujo mediante `procesoCompletoJob`.

---

# Conclusión

La solución implementa un proceso Batch capaz de recibir información proveniente de archivos CSV, validar y transformar sus registros, persistir información consistente y continuar su ejecución frente a determinados errores controlados.

La incorporación de excepciones personalizadas, políticas de `skip` y `retry`, listeners, parámetros configurables y procesamiento concurrente permite construir una solución más robusta, configurable y trazable.

Además, el uso de chunks de cinco registros junto con tres hilos de ejecución permite aplicar una estrategia básica de escalamiento para mejorar la capacidad de procesamiento de información, manteniendo al mismo tiempo reglas de validación y tolerancia a fallos.