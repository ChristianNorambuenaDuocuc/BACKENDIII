package cl.duoc.interesesservice.repository;

import cl.duoc.interesesservice.model.Interes;
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

@Repository
public class InteresRepository {

    public List<Interes> findAll() {

        List<Interes> intereses = new ArrayList<>();

        try {

            ClassPathResource resource =
                    new ClassPathResource("data/intereses.csv");

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

                Interes interes = new Interes();

                interes.setInteresesId(
                        parseInteger(record.get("intereses_id"))
                );

                interes.setNombre(
                        record.get("nombre")
                );

                interes.setSaldo(
                        parseBigDecimal(record.get("saldo"))
                );

                interes.setEdad(
                        parseInteger(record.get("edad"))
                );

                interes.setTipo(
                        record.get("tipo")
                );

                intereses.add(interes);
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al leer el archivo intereses.csv",
                    e
            );
        }

        return intereses;
    }


    public List<Interes> findByCuentaId(Integer cuentaId) {

        return findAll()
                .stream()
                .filter(interes ->
                        interes.getInteresesId() != null &&
                        interes.getInteresesId().equals(cuentaId)
                )
                .toList();
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