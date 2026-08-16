package cl.duoc.semana1.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class BatchStepListener implements StepExecutionListener {

    private static final Logger logger =
            LoggerFactory.getLogger(BatchStepListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {

        logger.info(
                "INICIO STEP: {}",
                stepExecution.getStepName()
        );
    }

    @Override
    public ExitStatus afterStep(
            StepExecution stepExecution) {

        logger.info(
                "FIN STEP: {}",
                stepExecution.getStepName()
        );

        logger.info(
                "Leídos: {}",
                stepExecution.getReadCount()
        );

        logger.info(
                "Escritos: {}",
                stepExecution.getWriteCount()
        );

        logger.info(
                "Omitidos: {}",
                stepExecution.getSkipCount()
        );

        logger.info(
                "Estado: {}",
                stepExecution.getStatus()
        );

        return null;
    }
}
