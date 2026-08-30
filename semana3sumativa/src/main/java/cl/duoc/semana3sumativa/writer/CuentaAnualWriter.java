package cl.duoc.semana3sumativa.writer;


import cl.duoc.semana3sumativa.model.CuentaAnual;

import javax.sql.DataSource;

import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CuentaAnualWriter {

    @Bean
    public JdbcBatchItemWriter<CuentaAnual> cuentaAnualItemWriter(
            DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<CuentaAnual>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO cuentas_anuales
                        (cuenta_id, fecha, transaccion, monto, descripcion)
                        VALUES
                        (:cuentaId, :fecha, :transaccion, :monto, :descripcion)
                        """)
                .beanMapped()
                .build();
    }
}
