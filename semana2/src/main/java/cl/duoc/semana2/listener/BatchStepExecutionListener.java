package cl.duoc.semana2.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                "[Thread: {}] Iniciando step: {}",
                Thread.currentThread().getName(),
                stepExecution.getStepName()
        );
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        logger.info(
                "[Thread: {}] Finalizó step: {} | leídos: {} | escritos: {} | omitidos: {} | estado: {}",
                Thread.currentThread().getName(),
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount(),
                stepExecution.getExitStatus()
        );

        return stepExecution.getExitStatus();
    }
}