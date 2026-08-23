package cl.duoc.semana2.reader;


import cl.duoc.semana2.dto.CuentaAnualCsv;

import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class CuentaAnualReader {

    @Bean
    public FlatFileItemReader<CuentaAnualCsv> cuentaAnualItemReader() {

        return new FlatFileItemReaderBuilder<CuentaAnualCsv>()
                .name("cuentaAnualItemReader")
                .resource(new ClassPathResource("data/cuentas_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names(
                        "cuentaId",
                        "fecha",
                        "transaccion",
                        "monto",
                        "descripcion"
                )
                .targetType(CuentaAnualCsv.class)
                .build();
    }
}