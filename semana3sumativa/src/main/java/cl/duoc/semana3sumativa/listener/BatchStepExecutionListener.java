package cl.duoc.semana3sumativa.listener;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class BatchStepExecutionListener implements StepExecutionListener {

    private static final Logger logger =
            LoggerFactory.getLogger(BatchStepExecutionListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {

        logger.info(
                "[BATCH-AUDIT] evento=STEP_INICIO | jobExecutionId={} | step={} | inicio={} | thread={}",
                stepExecution.getJobExecutionId(),
                stepExecution.getStepName(),
                stepExecution.getStartTime(),
                Thread.currentThread().getName()
        );
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        long duracionMs = 0;

        LocalDateTime inicio = stepExecution.getStartTime();

        if (inicio != null) {
            duracionMs = Duration.between(
                    inicio,
                    LocalDateTime.now()
            ).toMillis();
        }

        // Métricas estructuradas para auditoría
        logger.info(
                "[BATCH-AUDIT] evento=STEP_FIN | jobExecutionId={} | step={} | estado={} | exitCode={} | leidos={} | escritos={} | omitidos={} | omitidosLectura={} | omitidosProceso={} | omitidosEscritura={} | filtrados={} | commits={} | rollbacks={} | duracionMs={} | thread={}",
                stepExecution.getJobExecutionId(),
                stepExecution.getStepName(),
                stepExecution.getStatus(),
                stepExecution.getExitStatus().getExitCode(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount(),
                stepExecution.getReadSkipCount(),
                stepExecution.getProcessSkipCount(),
                stepExecution.getWriteSkipCount(),
                stepExecution.getFilterCount(),
                stepExecution.getCommitCount(),
                stepExecution.getRollbackCount(),
                duracionMs,
                Thread.currentThread().getName()
        );

        // Alerta operacional si hubo registros omitidos
        if (stepExecution.getSkipCount() > 0) {

            logger.warn(
                    "[BATCH-ALERTA] step={} | registrosOmitidos={} | revisar datos inconsistentes",
                    stepExecution.getStepName(),
                    stepExecution.getSkipCount()
            );
        }

        // Alerta crítica si el Step falla
        if (stepExecution.getStatus() == BatchStatus.FAILED) {

            logger.error(
                    "[BATCH-ERROR] step={} | estado=FAILED | cantidadErrores={}",
                    stepExecution.getStepName(),
                    stepExecution.getFailureExceptions().size()
            );

            for (Throwable error : stepExecution.getFailureExceptions()) {

                logger.error(
                        "[BATCH-ERROR] step={} | tipo={} | mensaje={}",
                        stepExecution.getStepName(),
                        error.getClass().getSimpleName(),
                        error.getMessage()
                );
            }
        }

        return stepExecution.getExitStatus();
    }
}