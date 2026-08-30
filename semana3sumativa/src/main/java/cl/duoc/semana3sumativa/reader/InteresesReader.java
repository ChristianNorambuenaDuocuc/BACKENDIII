package cl.duoc.semana3sumativa.reader;

import cl.duoc.semana3sumativa.dto.InteresCsv;

import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class InteresesReader {

    @Bean
    public FlatFileItemReader<InteresCsv> interesesItemReader() {

        return new FlatFileItemReaderBuilder<InteresCsv>()
                .name("interesesItemReader")
                .resource(new ClassPathResource("data/intereses.csv"))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names(
                        "cuentaId",
                        "nombre",
                        "saldo",
                        "edad",
                        "tipo"
                )
                .targetType(InteresCsv.class)
                .build();
    }
}