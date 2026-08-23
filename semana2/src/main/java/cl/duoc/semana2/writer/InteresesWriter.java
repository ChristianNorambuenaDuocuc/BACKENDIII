package cl.duoc.semana2.writer;

import cl.duoc.semana2.model.Intereses;

import javax.sql.DataSource;

import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InteresesWriter {

    @Bean
    public JdbcBatchItemWriter<Intereses> interesesItemWriter(
            DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Intereses>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO intereses
                        (cuenta_id, nombre, saldo, edad, tipo)
                        VALUES
                        (:cuentaId, :nombre, :saldo, :edad, :tipo)
                        """)
                .beanMapped()
                .build();
    }
}
