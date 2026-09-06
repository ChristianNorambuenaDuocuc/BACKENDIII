package cl.duoc.transaccionesservice.repository;

import cl.duoc.transaccionesservice.model.Transaccion;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TransaccionRepository {

    public List<Transaccion> findAll() {

        List<Transaccion> transacciones = new ArrayList<>();

        try {

            ClassPathResource resource =
                    new ClassPathResource("data/transacciones.csv");

            Reader reader = new InputStreamReader(
                    resource.getInputStream(),
                    StandardCharsets.UTF_8
            );

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .get();

            Iterable<CSVRecord> records =
                    format.parse(reader);

            for (CSVRecord record : records) {

                Transaccion transaccion = new Transaccion();

                transaccion.setId(
                        parseInteger(record.get("id"))
                );

                transaccion.setFecha(
                        record.get("fecha")
                );

                transaccion.setMonto(
                        parseBigDecimal(record.get("monto"))
                );

                transaccion.setTipo(
                        record.get("tipo")
                );

                transacciones.add(transaccion);
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al leer el archivo transacciones.csv",
                    e
            );
        }

        return transacciones;
    }


    public Optional<Transaccion> findById(Integer id) {

        return findAll()
                .stream()
                .filter(transaccion ->
                        transaccion.getId() != null &&
                        transaccion.getId().equals(id)
                )
                .findFirst();
    }


    private Integer parseInteger(String valor) {

        try {

            if (valor == null || valor.isBlank()) {
                return null;
            }

            return Integer.valueOf(valor);

        } catch (NumberFormatException e) {

            return null;
        }
    }


    private BigDecimal parseBigDecimal(String valor) {

        try {

            if (valor == null || valor.isBlank()) {
                return null;
            }

            return new BigDecimal(valor);

        } catch (NumberFormatException e) {

            return null;
        }
    }
}