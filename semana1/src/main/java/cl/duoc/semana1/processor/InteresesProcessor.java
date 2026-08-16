package cl.duoc.semana1.processor;


import cl.duoc.semana1.dto.InteresCsv;
import cl.duoc.semana1.exception.InteresInvalidoException;
import cl.duoc.semana1.model.Intereses;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class InteresesProcessor
        implements ItemProcessor<InteresCsv, Intereses> {

    // Tasas de ejemplo.
    // Se pueden cambiar después según las reglas del proyecto.
    private static final BigDecimal TASA_AHORRO =
            new BigDecimal("0.01"); // 1%

    private static final BigDecimal TASA_PRESTAMO =
            new BigDecimal("0.02"); // 2%

    @Override
    public Intereses process(InteresCsv item) throws Exception {

        Intereses interes = new Intereses();

        // =========================
        // CUENTA ID
        // =========================

        if (item.getCuentaId() == null ||
                item.getCuentaId().isBlank()) {

            throw new InteresInvalidoException(
                    "Cuenta ID vacía"
            );
        }

        try {
            interes.setCuentaId(
                    Long.parseLong(item.getCuentaId())
            );
        } catch (NumberFormatException e) {

            throw new InteresInvalidoException(
                    "Cuenta ID inválida: "
                            + item.getCuentaId()
            );
        }


        // =========================
        // NOMBRE
        // =========================

        if (item.getNombre() == null ||
                item.getNombre().isBlank()) {

            throw new InteresInvalidoException(
                    "Nombre vacío"
            );
        }

        interes.setNombre(
                item.getNombre().trim()
        );


        // =========================
        // EDAD
        // =========================

        Integer edad;

        if (item.getEdad() == null ||
                item.getEdad().isBlank()) {

            throw new InteresInvalidoException(
                    "Edad vacía"
            );
        }

        try {
            edad = Integer.parseInt(item.getEdad());
        } catch (NumberFormatException e) {

            throw new InteresInvalidoException(
                    "Edad inválida: " + item.getEdad()
            );
        }

        if (edad < 18 || edad > 100) {

            throw new InteresInvalidoException(
                    "Edad fuera de rango: " + edad
            );
        }

        interes.setEdad(edad);


        // =========================
        // SALDO
        // =========================

        if (item.getSaldo() == null ||
                item.getSaldo().isBlank()) {

            throw new InteresInvalidoException(
                    "Saldo vacío"
            );
        }

        BigDecimal saldo;

        try {

            saldo = new BigDecimal(
                    item.getSaldo()
            );

        } catch (NumberFormatException e) {

            throw new InteresInvalidoException(
                    "Saldo inválido: "
                            + item.getSaldo()
            );

            }

            if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InteresInvalidoException(
            "El saldo debe ser mayor que cero: " + saldo
            );}

        


        // =========================
        // TIPO DE CUENTA
        // =========================

        if (item.getTipo() == null ||
                item.getTipo().isBlank()) {

            throw new InteresInvalidoException(
                    "Tipo de cuenta vacío"
            );
        }

        String tipo =
                item.getTipo()
                        .trim()
                        .toLowerCase();

        if (!tipo.equals("ahorro")
                && !tipo.equals("prestamo")) {

            throw new InteresInvalidoException(
                    "Tipo de cuenta inválido: "
                            + item.getTipo()
            );
        }


        // =========================
        // CÁLCULO DE INTERÉS
        // =========================

        BigDecimal interesCalculado;

        if (tipo.equals("ahorro")) {

            interesCalculado =
                    saldo.multiply(TASA_AHORRO);

            saldo =
                    saldo.add(interesCalculado);

        } else {

            interesCalculado =
                    saldo.multiply(TASA_PRESTAMO);

            saldo =
                    saldo.add(interesCalculado);
        }

        saldo = saldo.setScale(
                2,
                RoundingMode.HALF_UP
        );


        interes.setSaldo(saldo);
        interes.setTipo(tipo);

        return interes;
    }
}