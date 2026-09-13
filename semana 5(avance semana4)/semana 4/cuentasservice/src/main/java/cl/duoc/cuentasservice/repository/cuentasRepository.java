package cl.duoc.cuentasservice.repository;


import cl.duoc.cuentasservice.model.cuentas;
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
public class cuentasRepository {

    public List<cuentas> findAll() {

        List<cuentas> cuentass = new ArrayList<>();

        try {

            ClassPathResource resource =
                    new ClassPathResource("data/cuentas_anuales.csv");

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

            Iterable<CSVRecord> records = format.parse(reader);

            for (CSVRecord record : records) {

                cuentas cuentas = new cuentas();

                cuentas.setcuentasId(
                        parseInteger(record.get("cuentas_id"))
                );

                cuentas.setFecha(
                        record.get("fecha")
                );

                cuentas.setTransaccion(
                        record.get("transaccion")
                );

                cuentas.setMonto(
                        parseBigDecimal(record.get("monto"))
                );

                cuentas.setDescripcion(
                        record.get("descripcion")
                );

                cuentass.add(cuentas);
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al leer el archivo cuentas_anuales.csv",
                    e
            );
        }

        return cuentass;
    }

    public List<cuentas> findBycuentasId(Integer cuentasId) {

        return findAll()
                .stream()
                .filter(cuentas ->
                        cuentas.getcuentasId() != null &&
                        cuentas.getcuentasId().equals(cuentasId)
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
