package cl.duoc.semana1.config;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.PlatformTransactionManager;

import cl.duoc.semana1.dto.CuentaAnualCsv;
import cl.duoc.semana1.dto.InteresCsv;
import cl.duoc.semana1.dto.TransaccionCsv;

import cl.duoc.semana1.model.CuentaAnual;
import cl.duoc.semana1.model.Intereses;
import cl.duoc.semana1.model.Transacciones;

import cl.duoc.semana1.exception.CuentaAnualInvalidaException;
import cl.duoc.semana1.exception.InteresInvalidoException;
import cl.duoc.semana1.exception.TransaccionInvalidaException;

import cl.duoc.semana1.listener.CuentaAnualSkipListener;
import cl.duoc.semana1.listener.InteresesSkipListener;
import cl.duoc.semana1.listener.TransaccionesSkipListener;
import cl.duoc.semana1.tasklet.ResumenTransaccionesTasklet;
import cl.duoc.semana1.tasklet.ResumenCuentaAnualTasklet;




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
            TransaccionesSkipListener transaccionesSkipListener) {

        return new StepBuilder(
                "transaccionesStep",
                jobRepository)

                .<TransaccionCsv, Transacciones>chunk(10)

                .transactionManager(transactionManager)

                .reader(transaccionesItemReader)

                .processor(transaccionesProcessor)

                .writer(transaccionesItemWriter)

                // Manejo de errores
                .faultTolerant()

                .skip(TransaccionInvalidaException.class)

                .skipLimit(20)

                // Listener para registros omitidos
                .listener(transaccionesSkipListener)

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
            InteresesSkipListener interesesSkipListener) {

        return new StepBuilder(
                "interesesStep",
                jobRepository)

                .<InteresCsv, Intereses>chunk(10)
                .transactionManager(transactionManager)

                .reader(interesesItemReader)

                .processor(interesesProcessor)

                .writer(interesesItemWriter)

                // Manejo de errores
                .faultTolerant()

                .skip(InteresInvalidoException.class)

                .skipLimit(20)

                // Listener para registros omitidos
                .listener(interesesSkipListener)

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
            CuentaAnualSkipListener cuentaAnualSkipListener) {

        return new StepBuilder(
                "cuentasAnualesStep",
                jobRepository)

                .<CuentaAnualCsv, CuentaAnual>chunk(10)
                .transactionManager(transactionManager)

                .reader(cuentaAnualItemReader)

                .processor(cuentasAnualesProcessor)

                .writer(cuentaAnualItemWriter)

                // Manejo de errores
                .faultTolerant()

                .skip(CuentaAnualInvalidaException.class)

                .skipLimit(20)

                // Listener para registros omitidos
                .listener(cuentaAnualSkipListener)

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

}