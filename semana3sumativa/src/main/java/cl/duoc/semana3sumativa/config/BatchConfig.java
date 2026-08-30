package cl.duoc.semana3sumativa.config;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.PlatformTransactionManager;

import cl.duoc.semana3sumativa.dto.CuentaAnualCsv;
import cl.duoc.semana3sumativa.dto.InteresCsv;
import cl.duoc.semana3sumativa.dto.TransaccionCsv;

import cl.duoc.semana3sumativa.model.CuentaAnual;
import cl.duoc.semana3sumativa.model.Intereses;
import cl.duoc.semana3sumativa.model.Transacciones;

import cl.duoc.semana3sumativa.exception.CuentaAnualInvalidaException;
import cl.duoc.semana3sumativa.exception.InteresInvalidoException;
import cl.duoc.semana3sumativa.exception.TransaccionInvalidaException;

import cl.duoc.semana3sumativa.listener.CuentaAnualSkipListener;
import cl.duoc.semana3sumativa.listener.InteresesSkipListener;
import cl.duoc.semana3sumativa.listener.TransaccionesSkipListener;
import cl.duoc.semana3sumativa.listener.BatchStepExecutionListener;
import cl.duoc.semana3sumativa.tasklet.ResumenTransaccionesTasklet;
import cl.duoc.semana3sumativa.tasklet.ResumenCuentaAnualTasklet;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Value;
import java.time.format.DateTimeParseException;

import org.springframework.batch.core.step.skip.SkipPolicy;
import java.time.Duration;

import org.springframework.core.retry.RetryPolicy;





@Configuration
public class BatchConfig {

        @Value("${batch.backoff.initial-interval}")
        private long backoffInitialInterval;

        @Value("${batch.backoff.multiplier}")
        private double backoffMultiplier;

        @Value("${batch.backoff.max-interval}")
        private long backoffMaxInterval;

        @Value("${batch.chunk-size}")
        private int chunkSize;

        @Value("${batch.skip-limit}")
        private int skipLimit;

        @Value("${batch.retry-limit}")
        private int retryLimit;

        @Value("${batch.executor.core-pool-size}")
        private int corePoolSize;

        @Value("${batch.executor.max-pool-size}")
        private int maxPoolSize;

        @Value("${batch.executor.queue-capacity}")
        private int queueCapacity;


    // =====================================================
    // JOB 1 - REPORTE DE TRANSACCIONES DIARIAS
    // =====================================================

   @Bean
public Job transaccionesJob(
        JobRepository jobRepository,
        Step transaccionesStep,
        Step resumenTransaccionesStep) {

    return new JobBuilder(
            "reporteTransaccionesJob",
            jobRepository)
            .start(transaccionesStep)
            .next(resumenTransaccionesStep)
            .build();
}


    // =====================================================
    // JOB 2 - CALCULO DE INTERESES MENSUALES
    // =====================================================

    @Bean
    public Job interesesJob(
            JobRepository jobRepository,
            Step interesesStep) {

        return new JobBuilder(
                "calculoInteresesJob",
                jobRepository)
                .start(interesesStep)
                .build();
    }


    // =====================================================
    // JOB 3 - ESTADOS DE CUENTA ANUALES
    // =====================================================

    @Bean
public Job cuentasAnualesJob(
        JobRepository jobRepository,
        Step cuentasAnualesStep,
        Step resumenCuentaAnualStep) {

    return new JobBuilder(
            "estadosCuentaAnualesJob",
            jobRepository)
            .start(cuentasAnualesStep)
            .next(resumenCuentaAnualStep)
            .build();
}


    // =====================================================
    // STEP TRANSACCIONES
    // =====================================================

    @Bean
    public Step transaccionesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<TransaccionCsv> transaccionesItemReader,
            ItemProcessor<TransaccionCsv, Transacciones> transaccionesProcessor,
            ItemWriter<Transacciones> transaccionesItemWriter,
            @Qualifier("batchTaskExecutor") ThreadPoolTaskExecutor taskExecutor,
            TransaccionesSkipListener transaccionesSkipListener,
            BatchStepExecutionListener batchStepExecutionListener) {

        return new StepBuilder(
                "transaccionesStep",jobRepository)

                .<TransaccionCsv, Transacciones>chunk(chunkSize)
                .transactionManager(transactionManager)


                .reader(transaccionesItemReader)

                .processor(transaccionesProcessor)

                .writer(transaccionesItemWriter)

                // Tolerancia a fallos:
                // - Skip para registros inválidos o inconsistentes.
                // - Retry para errores transitorios de base de datos.

                .faultTolerant()

                // Registros inválidos: se omiten y el procesamiento continúa.

                .skipPolicy(skipPolicy())

                // Errores temporales de BD: se reintenta la operación. 

                .retryPolicy(retryPolicy())
                

                // Listener para registros omitidos
                .listener(transaccionesSkipListener)

                .listener(batchStepExecutionListener)

                .taskExecutor(taskExecutor)

                .build();
    }

       @Bean
public Step resumenTransaccionesStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ResumenTransaccionesTasklet resumenTransaccionesTasklet) {

    return new StepBuilder(
            "resumenTransaccionesStep",
            jobRepository)
            .tasklet(resumenTransaccionesTasklet)
            .transactionManager(transactionManager)
            .build();
} 



    // =====================================================
    // STEP INTERESES
    // =====================================================

    @Bean
    public Step interesesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<InteresCsv> interesesItemReader,
            ItemProcessor<InteresCsv, Intereses> interesesProcessor,
            ItemWriter<Intereses> interesesItemWriter,
            @Qualifier("batchTaskExecutor") ThreadPoolTaskExecutor taskExecutor,
            InteresesSkipListener interesesSkipListener,
            BatchStepExecutionListener batchStepExecutionListener) {

        return new StepBuilder(
                "interesesStep",
                jobRepository)

                .<InteresCsv, Intereses>chunk(chunkSize)
                .transactionManager(transactionManager)

                .reader(interesesItemReader)

                .processor(interesesProcessor)

                .writer(interesesItemWriter)

                // Tolerancia a fallos:
                // - Skip para registros inválidos o inconsistentes.
                // - Retry para errores transitorios de base de datos.
                .faultTolerant()

                // Registros inválidos: se omiten y el procesamiento continúa.

                .skipPolicy(skipPolicy())

                // Errores temporales de BD: se reintenta la operación.

                .retryPolicy(retryPolicy())

                // Listener para registros omitidos
                .listener(interesesSkipListener)

                .listener(batchStepExecutionListener)

                .taskExecutor(taskExecutor)

                .build();
    }


    // =====================================================
    // STEP CUENTAS ANUALES
    // =====================================================

    @Bean
    public Step cuentasAnualesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<CuentaAnualCsv> cuentaAnualItemReader,
            ItemProcessor<CuentaAnualCsv, CuentaAnual> cuentasAnualesProcessor,
            ItemWriter<CuentaAnual> cuentaAnualItemWriter,
            @Qualifier("batchTaskExecutor") ThreadPoolTaskExecutor taskExecutor,
            CuentaAnualSkipListener cuentaAnualSkipListener,
            BatchStepExecutionListener batchStepExecutionListener) {

        return new StepBuilder(
                "cuentasAnualesStep",
                jobRepository)

                .<CuentaAnualCsv, CuentaAnual>chunk(chunkSize)
                .transactionManager(transactionManager)
                
                .reader(cuentaAnualItemReader)

                .processor(cuentasAnualesProcessor)

                .writer(cuentaAnualItemWriter)

                // Tolerancia a fallos:
                // - Skip para registros inválidos o inconsistentes.
                // - Retry para errores transitorios de base de datos.
                .faultTolerant()

                // Registros inválidos: se omiten y el procesamiento continúa.

                .skipPolicy(skipPolicy())

                // Errores temporales de BD: se reintenta la operación.

                .retryPolicy(retryPolicy())

                // Listener para registros omitidos
                .listener(cuentaAnualSkipListener)

                .listener(batchStepExecutionListener)

                 .taskExecutor(taskExecutor)

                .build();
    }

    @Bean
public Step resumenCuentaAnualStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ResumenCuentaAnualTasklet resumenCuentaAnualTasklet) {

    return new StepBuilder(
            "resumenCuentaAnualStep",
            jobRepository)

            .tasklet(resumenCuentaAnualTasklet)

            .transactionManager(transactionManager)

            .build();
}

@Bean
public Job procesoCompletoJob(
        JobRepository jobRepository,
        Step transaccionesStep,
        Step resumenTransaccionesStep,
        Step interesesStep,
        Step cuentasAnualesStep,
        Step resumenCuentaAnualStep) {

    return new JobBuilder(
            "procesoCompletoJob",
            jobRepository)

            .start(transaccionesStep)
            .next(resumenTransaccionesStep)
            .next(interesesStep)
            .next(cuentasAnualesStep)
            .next(resumenCuentaAnualStep)

            .build();
}

// Spring Batch 6 utiliza un nuevo modelo de concurrencia.
// El límite de procesamiento paralelo se controla mediante
// maxPoolSize y una cola acotada en el TaskExecutor,
// evitando saturación sin utilizar el throttleLimit obsoleto.
@Bean(name = "batchTaskExecutor")
public ThreadPoolTaskExecutor batchTaskExecutor() {

    ThreadPoolTaskExecutor executor =
            new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix("Batch-");
    executor.setRejectedExecutionHandler(
        new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()
        );
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.initialize();

    return executor;
}

@Bean
public SkipPolicy skipPolicy() {

    return (throwable, skipCount) -> {

        // Si ya alcanzamos el límite configurado,
        // no se permiten más registros omitidos.
        if (skipCount >= skipLimit) {
            return false;
        }

        // Errores de validación propios del proyecto
        if (throwable instanceof TransaccionInvalidaException) {
            return true;
        }

        if (throwable instanceof InteresInvalidoException) {
            return true;
        }

        if (throwable instanceof CuentaAnualInvalidaException) {
            return true;
        }

        // Errores relacionados con datos del CSV
        if (throwable instanceof FlatFileParseException) {
            return true;
        }

        if (throwable instanceof DateTimeParseException) {
            return true;
        }

        if (throwable instanceof NumberFormatException) {
            return true;
        }

        // Duplicados que decidimos tolerar
        if (throwable instanceof DuplicateKeyException) {
            return true;
        }

        // Cualquier otro error NO se omite
        return false;
    };
}

@Bean
public RetryPolicy retryPolicy() {

    return RetryPolicy.builder()

            .includes(
                    CannotAcquireLockException.class,
                    TransientDataAccessException.class
            )

            .maxRetries(Math.max(0, retryLimit - 1))

            .delay(Duration.ofMillis(backoffInitialInterval))

            .multiplier(backoffMultiplier)

            .maxDelay(Duration.ofMillis(backoffMaxInterval))

            .build();
}



}