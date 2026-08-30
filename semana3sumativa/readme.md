# Procesamiento Bancario con Spring Batch

Proyecto desarrollado con **Spring Boot y Spring Batch** para procesar información bancaria proveniente de archivos CSV, aplicar reglas de validación y transformación, manejar registros inconsistentes y almacenar los resultados en una base de datos MySQL.

El proyecto corresponde a la continuidad de las actividades desarrolladas durante las semanas anteriores e incorpora **tolerancia a fallos, políticas personalizadas, procesamiento multi-thread, perfiles de configuración y trazabilidad operacional**.

---

## Tecnologías utilizadas

- Java 21
- Spring Boot 4.1.0
- Spring Batch
- Spring JDBC
- MySQL
- Maven
- MySQL Workbench
- Git
- GitHub

---

## Objetivo

Procesar datos bancarios provenientes de archivos CSV utilizando Spring Batch.

Cada archivo es procesado mediante el siguiente flujo:

```text
CSV
 ↓
ItemReader
 ↓
DTO
 ↓
ItemProcessor
 ↓
Modelo
 ↓
ItemWriter
 ↓
MySQL
```

Durante el procesamiento se aplican:

- Validaciones de datos.
- Transformaciones.
- Manejo de registros inconsistentes.
- Excepciones personalizadas.
- Política personalizada de Skip.
- Política personalizada de Retry.
- Tolerancia a fallos.
- Procesamiento multi-thread.
- Listeners.
- Auditoría y trazabilidad.
- Configuración mediante perfiles.

---

# Procesos Batch

El proyecto implementa tres procesos principales:

1. Reporte de Transacciones Diarias.
2. Cálculo de Intereses Mensuales.
3. Generación de Estados de Cuenta Anuales.

---

## 1. Reporte de Transacciones Diarias

Procesa información de transacciones bancarias desde un archivo CSV.

Flujo:

```text
TransaccionCsv
 ↓
TransaccionesProcessor
 ↓
Transacciones
 ↓
ItemWriter
 ↓
MySQL
```

Durante el procesamiento se validan datos como:

- Identificador de la transacción.
- Fecha.
- Monto.
- Tipo de transacción.

Los registros inválidos generan una `TransaccionInvalidaException`.

Por ejemplo, una transacción con monto menor o igual a cero es considerada inválida y puede ser omitida mediante la política personalizada de Skip.

---

## 2. Cálculo de Intereses Mensuales

Procesa información relacionada con clientes y cuentas.

Flujo:

```text
InteresCsv
 ↓
InteresesProcessor
 ↓
Intereses
 ↓
ItemWriter
 ↓
MySQL
```

Se realizan validaciones sobre:

- Número de cuenta.
- Saldo.
- Edad.
- Tipo de cuenta.
- Datos obligatorios del cliente.

Los registros inconsistentes generan una `InteresInvalidoException`.

---

## 3. Estados de Cuenta Anuales

Procesa las transacciones correspondientes a las cuentas anuales.

Flujo:

```text
CuentaAnualCsv
 ↓
CuentaAnualProcessor
 ↓
CuentaAnual
 ↓
ItemWriter
 ↓
MySQL
```

Se validan datos como:

- Número de cuenta.
- Fecha.
- Tipo de transacción.
- Monto.

Los registros incorrectos generan una `CuentaAnualInvalidaException`.

---

# Job principal

El proyecto incorpora un Job que permite ejecutar el procesamiento completo.

```text
procesoCompletoJob
        │
        ↓
transaccionesStep
        │
        ↓
resumenTransaccionesStep
        │
        ↓
interesesStep
        │
        ↓
cuentasAnualesStep
        │
        ↓
resumenCuentaAnualStep
```

Cada Step se ejecuta de forma secuencial dentro del Job principal.

---

# Manejo de errores y excepciones

Los archivos CSV pueden contener información incorrecta o mal clasificada.

Para manejar estos casos se utilizan excepciones personalizadas:

```text
TransaccionInvalidaException
InteresInvalidoException
CuentaAnualInvalidaException
```

Estas excepciones son generadas principalmente desde los `ItemProcessor` cuando un registro no cumple las reglas de negocio.

Esto permite diferenciar entre:

```text
Errores de datos
        ↓
pueden ser omitidos

Errores temporales
        ↓
pueden ser reintentados

Errores no recuperables
        ↓
detienen el Step
```

---

# Tolerancia a fallos

Los Steps utilizan la funcionalidad `faultTolerant()` de Spring Batch.

La configuración general utiliza:

```java
.faultTolerant()
.skipPolicy(skipPolicy())
.retryPolicy(retryPolicy())
```

Esto permite continuar el procesamiento cuando ocurren determinados errores recuperables.

---

# Política personalizada de Skip

Se implementó una `SkipPolicy` personalizada.

Su función es decidir qué excepciones pueden ser omitidas sin detener todo el procesamiento.

Entre las excepciones consideradas recuperables se encuentran:

```text
TransaccionInvalidaException
InteresInvalidoException
CuentaAnualInvalidaException
FlatFileParseException
DateTimeParseException
NumberFormatException
DuplicateKeyException
```

La lógica general es:

```text
Se produce una excepción
        ↓
¿Es una excepción recuperable?
        │
     ┌──┴──┐
     Sí    No
     ↓      ↓
   Skip   Step falla
```

También se controla una cantidad máxima de registros omitidos mediante:

```properties
batch.skip-limit
```

---

# Política personalizada de Retry

Se implementó una `RetryPolicy` para manejar errores temporales.

Los errores considerados reintentables corresponden principalmente a problemas transitorios relacionados con la base de datos:

```text
CannotAcquireLockException
TransientDataAccessException
```

La finalidad es permitir que una operación vuelva a intentarse cuando el error pueda desaparecer después de un breve período.

Los errores de validación de datos no se reintentan, ya que procesar nuevamente un dato incorrecto produciría el mismo resultado.

---

# Backoff entre reintentos

La política de Retry utiliza una espera progresiva entre cada intento.

Los parámetros son configurables:

```properties
batch.backoff.initial-interval
batch.backoff.multiplier
batch.backoff.max-interval
```

Ejemplo conceptual:

```text
Error temporal
      ↓
espera
      ↓
Retry
      ↓
vuelve a fallar
      ↓
aumenta el tiempo de espera
      ↓
Retry
```

Esto evita realizar reintentos continuos e inmediatos contra la base de datos.

---

# Skip Listeners

El proyecto incorpora listeners específicos para registrar los elementos omitidos:

```text
TransaccionesSkipListener
InteresesSkipListener
CuentaAnualSkipListener
```

Estos permiten identificar:

- Qué registro fue omitido.
- En qué etapa ocurrió.
- Qué datos contenía.
- Qué excepción produjo el Skip.
- Qué motivo generó el rechazo.

Ejemplo:

```text
Transacción omitida
ID: 3
Monto: -200
Motivo: El monto debe ser mayor que cero
```

---

# Auditoría y trazabilidad

Se implementó `BatchStepExecutionListener` para registrar información detallada sobre la ejecución de cada Step.

Al iniciar un Step se genera un evento:

```text
[BATCH-AUDIT] evento=STEP_INICIO
```

Al finalizar:

```text
[BATCH-AUDIT] evento=STEP_FIN
```

Las métricas registradas incluyen:

- Identificador de ejecución.
- Nombre del Step.
- Estado.
- Exit Code.
- Registros leídos.
- Registros escritos.
- Registros omitidos.
- Skips de lectura.
- Skips de procesamiento.
- Skips de escritura.
- Registros filtrados.
- Commits.
- Rollbacks.
- Duración del Step.
- Thread de ejecución.

Cuando existen registros omitidos se genera además una alerta:

```text
[BATCH-ALERTA]
```

Ejemplo:

```text
[BATCH-ALERTA] step=transaccionesStep |
registrosOmitidos=2 |
revisar datos inconsistentes
```

---

# Política de escalamiento

Para mejorar la capacidad de procesamiento se seleccionó una estrategia **multi-thread**.

La actividad permite seleccionar entre:

- Multi-thread.
- Particionamiento.

En este proyecto se eligió **multi-thread**, debido a que los registros pueden ser procesados de manera concurrente.

Los Steps utilizan:

```java
.taskExecutor(taskExecutor)
```

---

# ThreadPoolTaskExecutor

El procesamiento concurrente se configura mediante `ThreadPoolTaskExecutor`.

Los principales parámetros son:

```properties
batch.executor.core-pool-size
batch.executor.max-pool-size
batch.executor.queue-capacity
```

El executor utiliza además el prefijo:

```text
Batch-
```

Esto permite identificar claramente los hilos utilizados durante la ejecución.

Por ejemplo:

```text
[Thread: Batch-1] Procesando transacción ID: 1
[Thread: Batch-2] Procesando transacción ID: 2
[Thread: Batch-3] Procesando transacción ID: 3
```

---

# Control de saturación

El `ThreadPoolTaskExecutor` utiliza:

```java
ThreadPoolExecutor.CallerRunsPolicy
```

como política de rechazo de tareas.

Cuando el pool de threads y la cola alcanzan su capacidad máxima, `CallerRunsPolicy` permite que la tarea sea ejecutada por el mismo thread que intentó enviarla.

Esto ayuda a disminuir la presión sobre el executor y evita un crecimiento descontrolado de tareas pendientes.

---

# Configuración externalizada

Los parámetros operativos fueron externalizados desde el código Java hacia archivos de propiedades.

Esto permite modificar el comportamiento de la aplicación sin cambiar directamente la lógica del programa.

Entre los parámetros configurables se encuentran:

```properties
batch.chunk-size
batch.skip-limit
batch.retry-limit

batch.executor.core-pool-size
batch.executor.max-pool-size
batch.executor.queue-capacity

batch.backoff.initial-interval
batch.backoff.multiplier
batch.backoff.max-interval
```

---

# Perfiles

Se utilizan perfiles de Spring Boot para separar configuraciones según el ambiente.

El proyecto contiene:

```text
application.properties
application-dev.properties
application-prod.properties
```

El perfil activo puede configurarse mediante:

```properties
spring.profiles.active=dev
```

---

## Perfil de desarrollo

Ejemplo de configuración:

```properties
batch.chunk-size=5
batch.skip-limit=20
batch.retry-limit=3

batch.executor.core-pool-size=3
batch.executor.max-pool-size=3
batch.executor.queue-capacity=25

batch.backoff.initial-interval=1000
batch.backoff.multiplier=2.0
batch.backoff.max-interval=10000
```

Esta configuración utiliza una cantidad controlada de threads y chunks pequeños para facilitar las pruebas.

---

## Perfil de producción

El perfil de producción permite utilizar parámetros distintos sin modificar el código Java.

Por ejemplo, puede configurarse con:

```text
Chunks de mayor tamaño
Mayor cantidad de threads
Mayor capacidad de cola
Mayor límite de registros procesados
```

De esta manera la aplicación puede adaptarse a distintos volúmenes de información.

---

# Base de datos

El proyecto utiliza **MySQL** como base de datos relacional.

La base de datos utilizada es:

```text
banco_batch
```

Los registros procesados son persistidos mediante `ItemWriter` y Spring JDBC.

---

# Arquitectura general

```text
                   procesoCompletoJob
                           │
                           ↓
                    Spring Batch
                           │
        ┌──────────────────┼──────────────────┐
        ↓                  ↓                  ↓
 Transacciones         Intereses        Cuentas Anuales
        │                  │                  │
        ↓                  ↓                  ↓
     Reader             Reader             Reader
        │                  │                  │
        ↓                  ↓                  ↓
    Processor          Processor          Processor
        │                  │                  │
        ↓                  ↓                  ↓
     Writer             Writer             Writer
        │                  │                  │
        └──────────────────┼──────────────────┘
                           ↓
                         MySQL

                    Componentes
                         adicionales
                           │
          ┌────────────────┼────────────────┐
          ↓                ↓                ↓
      SkipPolicy       RetryPolicy      Listeners
          │                │                │
          ↓                ↓                ↓
   Datos inválidos    Errores BD       Auditoría
          │                │
          ↓                ↓
        Skip             Retry

                           +
                           │
                           ↓
                  ThreadPoolTaskExecutor
                           │
                           ↓
                    Multi-thread
```

---

# Ejemplo de ejecución

Durante una ejecución se pueden observar distintos threads procesando registros:

```text
[Thread: Batch-1] Procesando transacción ID: 1
[Thread: Batch-2] Procesando transacción ID: 2
[Thread: Batch-3] Procesando transacción ID: 3
```

También se detectan registros inválidos:

```text
Transacción omitida
id: 3
monto: -200
Motivo: El monto debe ser mayor que cero
```

El procesamiento continúa con el resto de los registros válidos.

---

# Resultado de prueba

Una ejecución del Job completo obtuvo los siguientes resultados:

## Transacciones

```text
Leídos: 10
Escritos: 8
Omitidos: 2
Estado: COMPLETED
```

## Intereses

```text
Leídos: 8
Escritos: 6
Omitidos: 2
Estado: COMPLETED
```

## Cuentas Anuales

```text
Leídos: 9
Escritos: 8
Omitidos: 1
Estado: COMPLETED
```

Resultado final:

```text
Job: procesoCompletoJob
Status: COMPLETED
```

---

# Estrategia de diseño

La solución separa las responsabilidades del procesamiento:

```text
ItemReader
    ↓
Lectura de datos

ItemProcessor
    ↓
Validación y transformación

SkipPolicy
    ↓
Decisión sobre errores recuperables

RetryPolicy
    ↓
Reintentos de errores temporales

ItemWriter
    ↓
Persistencia en MySQL

SkipListener
    ↓
Registro de elementos omitidos

BatchStepExecutionListener
    ↓
Auditoría y métricas

ThreadPoolTaskExecutor
    ↓
Procesamiento multi-thread
```

Esta arquitectura permite procesar información bancaria de manera controlada, tolerar registros inconsistentes, manejar errores temporales y aumentar la capacidad de procesamiento mediante concurrencia.

---

# Ejecución del proyecto

En Windows, utilizando Maven Wrapper:

```bash
.\mvnw.cmd spring-boot:run
```

También puede ejecutarse desde Visual Studio Code utilizando la clase principal de Spring Boot.

---

# Control de versiones

El proyecto utiliza Git y GitHub para el control de versiones.

Para verificar los cambios:

```bash
git status
```

Agregar los archivos:

```bash
git add .
```

Crear un commit:

```bash
git commit -m "Implementa politicas personalizadas y escalamiento multithread"
```

Subir los cambios:

```bash
git push
```

---

# Autor

Proyecto desarrollado como parte de la asignatura **Backend III**, utilizando Java, Spring Boot, Spring Batch y MySQL.