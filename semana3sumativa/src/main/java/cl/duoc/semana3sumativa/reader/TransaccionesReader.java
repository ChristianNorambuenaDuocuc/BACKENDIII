package cl.duoc.semana3sumativa.reader;


import cl.duoc.semana3sumativa.dto.TransaccionCsv;

import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class TransaccionesReader {

    @Bean
    public FlatFileItemReader<TransaccionCsv> transaccionesItemReader() {

        return new FlatFileItemReaderBuilder<TransaccionCsv>()
                .name("transaccionesItemReader")
                .resource(new ClassPathResource("data/transacciones.csv"))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("id", "fecha", "monto", "tipo")
                .targetType(TransaccionCsv.class)
                .build();
    }
}
