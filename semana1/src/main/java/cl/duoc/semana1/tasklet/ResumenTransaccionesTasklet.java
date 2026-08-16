package cl.duoc.semana1.tasklet;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ResumenTransaccionesTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public ResumenTransaccionesTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) throws Exception {

        // Limpiamos el resumen anterior
        jdbcTemplate.update(
                "DELETE FROM resumen_transacciones_diarias"
        );

        // Generamos nuevamente el resumen diario
        jdbcTemplate.update("""
                INSERT INTO resumen_transacciones_diarias
                (
                    fecha,
                    cantidad_transacciones,
                    total_creditos,
                    total_debitos,
                    monto_total
                )
                SELECT
                    fecha,
                    COUNT(*),
                    SUM(
                        CASE
                            WHEN tipo = 'credito'
                            THEN monto
                            ELSE 0
                        END
                    ),
                    SUM(
                        CASE
                            WHEN tipo = 'debito'
                            THEN monto
                            ELSE 0
                        END
                    ),
                    SUM(monto)
                FROM transacciones
                GROUP BY fecha
                """);

        return RepeatStatus.FINISHED;
    }
}