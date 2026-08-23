package cl.duoc.semana2.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class BatchJobListener implements JobExecutionListener {

    private static final Logger logger =
            LoggerFactory.getLogger(BatchJobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {

        logger.info(
                "INICIO JOB: {}",
                jobExecution.getJobInstance().getJobName()
        );
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        logger.info(
                "FIN JOB: {} - ESTADO: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus()
        );

        if (!jobExecution.getAllFailureExceptions().isEmpty()) {

            jobExecution.getAllFailureExceptions()
                    .forEach(error ->
                            logger.error(
                                    "Error en Job: {}",
                                    error.getMessage()
                            )
                    );
        }
    }
}
