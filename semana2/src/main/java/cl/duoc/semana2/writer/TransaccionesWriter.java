package cl.duoc.semana2.writer;

import cl.duoc.semana2.model.Transacciones;

import javax.sql.DataSource;

import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransaccionesWriter {

    @Bean
    public JdbcBatchItemWriter<Transacciones> transaccionesItemWriter(
            DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Transacciones>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO transacciones
                        (id, fecha, monto, tipo)
                        VALUES
                        (:id, :fecha, :monto, :tipo)
                        """)
                .beanMapped()
                .build();
    }
}
