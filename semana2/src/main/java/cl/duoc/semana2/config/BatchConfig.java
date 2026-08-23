package cl.duoc.semana2.config;

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

import cl.duoc.semana2.dto.CuentaAnualCsv;
import cl.duoc.semana2.dto.InteresCsv;
import cl.duoc.semana2.dto.TransaccionCsv;

import cl.duoc.semana2.model.CuentaAnual;
import cl.duoc.semana2.model.Intereses;
import cl.duoc.semana2.model.Transacciones;

import cl.duoc.semana2.exception.CuentaAnualInvalidaException;
import cl.duoc.semana2.exception.InteresInvalidoException;
import cl.duoc.semana2.exception.TransaccionInvalidaException;

import cl.duoc.semana2.listener.CuentaAnualSkipListener;
import cl.duoc.semana2.listener.InteresesSkipListener;
import cl.duoc.semana2.listener.TransaccionesSkipListener;
import cl.duoc.semana2.listener.BatchStepExecutionListener;
import cl.duoc.semana2.tasklet.ResumenTransaccionesTasklet;
import cl.duoc.semana2.tasklet.ResumenCuentaAnualTasklet;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;






@Configuration
public class BatchConfig {


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

                .<TransaccionCsv, Transacciones>chunk(5, transactionManager)


                .reader(transaccionesItemReader)

                .processor(transaccionesProcessor)

                .writer(transaccionesItemWriter)

                // Tolerancia a fallos:
                // - Skip para registros inválidos o inconsistentes.
                // - Retry para errores transitorios de base de datos.

                .faultTolerant()

                // Registros inválidos: se omiten y el procesamiento continúa.

                .skip(TransaccionInvalidaException.class)

                .skip(DuplicateKeyException.class)
                
                .skip(FlatFileParseException.class)

                .skipLimit(20)

                // Errores temporales de BD: se reintenta la operación. 

                .retryLimit(3)
                .retry(CannotAcquireLockException.class)
                .retry(TransientDataAccessException.class)

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

                .<InteresCsv, Intereses>chunk(5, transactionManager)

                .reader(interesesItemReader)

                .processor(interesesProcessor)

                .writer(interesesItemWriter)

                // Tolerancia a fallos:
                // - Skip para registros inválidos o inconsistentes.
                // - Retry para errores transitorios de base de datos.
                .faultTolerant()

                // Registros inválidos: se omiten y el procesamiento continúa.

                .skip(InteresInvalidoException.class)

                .skip(DuplicateKeyException.class)

                .skip(FlatFileParseException.class)

                .skipLimit(20)

                // Errores temporales de BD: se reintenta la operación.

                .retryLimit(3)
                .retry(CannotAcquireLockException.class)
                .retry(TransientDataAccessException.class)

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

                .<CuentaAnualCsv, CuentaAnual>chunk(5, transactionManager)
                
                .reader(cuentaAnualItemReader)

                .processor(cuentasAnualesProcessor)

                .writer(cuentaAnualItemWriter)

                // Tolerancia a fallos:
                // - Skip para registros inválidos o inconsistentes.
                // - Retry para errores transitorios de base de datos.
                .faultTolerant()

                // Registros inválidos: se omiten y el procesamiento continúa.

                .skip(CuentaAnualInvalidaException.class)

                .skip(FlatFileParseException.class)

                .skipLimit(20)

                // Errores temporales de BD: se reintenta la operación.

                .retryLimit(3)
                .retry(CannotAcquireLockException.class)
                .retry(TransientDataAccessException.class)

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

@Bean(name = "batchTaskExecutor")
public ThreadPoolTaskExecutor batchTaskExecutor() {

    ThreadPoolTaskExecutor executor =
            new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(3);
    executor.setQueueCapacity(25);
    executor.setThreadNamePrefix("Batch-");
    executor.initialize();

    return executor;
}

}