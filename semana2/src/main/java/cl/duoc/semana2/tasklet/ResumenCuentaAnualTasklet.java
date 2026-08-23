package cl.duoc.semana2.tasklet;


import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ResumenCuentaAnualTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public ResumenCuentaAnualTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) throws Exception {

        // Limpiamos el resumen anterior
        jdbcTemplate.update(
                "DELETE FROM resumen_cuentas_anuales"
        );

        // Generamos el resumen anual por cuenta
        jdbcTemplate.update("""
                INSERT INTO resumen_cuentas_anuales
                (
                    cuenta_id,
                    cantidad_transacciones,
                    total_depositos,
                    total_retiros,
                    saldo_anual
                )
                SELECT
                    cuenta_id,
                    COUNT(*),

                    SUM(
                        CASE
                            WHEN transaccion = 'deposito'
                            THEN monto
                            ELSE 0
                        END
                    ),

                    SUM(
                        CASE
                            WHEN transaccion IN ('retiro', 'compra', 'pago')
                            THEN monto
                            ELSE 0
                        END
                    ),

                    SUM(
                        CASE
                            WHEN transaccion = 'deposito'
                            THEN monto
                            WHEN transaccion IN ('retiro', 'compra', 'pago')
                            THEN -monto
                            ELSE 0
                        END
                    )

                FROM cuentas_anuales

                GROUP BY cuenta_id
                """);

        return RepeatStatus.FINISHED;
    }
}