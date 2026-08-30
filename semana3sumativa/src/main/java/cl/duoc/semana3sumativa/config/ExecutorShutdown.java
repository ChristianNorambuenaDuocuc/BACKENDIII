package cl.duoc.semana3sumativa.config;

import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class ExecutorShutdown {

    private static final Logger logger =
            LoggerFactory.getLogger(ExecutorShutdown.class);

    private final ThreadPoolTaskExecutor taskExecutor;

    public ExecutorShutdown(
            @Qualifier("batchTaskExecutor")
            ThreadPoolTaskExecutor taskExecutor) {

        this.taskExecutor = taskExecutor;
    }

    @PreDestroy
    public void shutdown() {

        logger.info(
                "Cerrando ThreadPoolTaskExecutor. Pool activo: {}, hilos activos: {}",
                taskExecutor.getPoolSize(),
                taskExecutor.getActiveCount()
        );

        taskExecutor.shutdown();

        logger.info("ThreadPoolTaskExecutor cerrado correctamente.");
    }
}
