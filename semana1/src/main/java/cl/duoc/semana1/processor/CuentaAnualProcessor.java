package cl.duoc.semana1.processor;

import cl.duoc.semana1.dto.CuentaAnualCsv;
import cl.duoc.semana1.exception.CuentaAnualInvalidaException;
import cl.duoc.semana1.model.CuentaAnual;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class CuentaAnualProcessor
        implements ItemProcessor<CuentaAnualCsv, CuentaAnual> {

    @Override
    public CuentaAnual process(
            CuentaAnualCsv item) throws Exception {

        CuentaAnual cuenta =
                new CuentaAnual();


        // =========================
        // CUENTA ID
        // =========================

        if (item.getCuentaId() == null ||
                item.getCuentaId().isBlank()) {

            throw new CuentaAnualInvalidaException(
                    "Cuenta ID vacía"
            );
        }

        try {

            cuenta.setCuentaId(
                    Long.parseLong(
                            item.getCuentaId()
                    )
            );

        } catch (NumberFormatException e) {

            throw new CuentaAnualInvalidaException(
                    "Cuenta ID inválida: "
                            + item.getCuentaId()
            );
        }


        // =========================
        // FECHA
        // =========================

        LocalDate fecha =
                convertirFecha(
                        item.getFecha()
                );

        cuenta.setFecha(fecha);


        // =========================
        // TRANSACCIÓN
        // =========================

        if (item.getTransaccion() == null ||
                item.getTransaccion().isBlank()) {

            throw new CuentaAnualInvalidaException(
                    "Transacción vacía"
            );
        }

        String transaccion =
                item.getTransaccion()
                        .trim()
                        .toLowerCase();

        cuenta.setTransaccion(transaccion);


        // =========================
        // MONTO
        // =========================

        if (item.getMonto() == null ||
                item.getMonto().isBlank()) {

            throw new CuentaAnualInvalidaException(
                    "Monto vacío"
            );
        }

        BigDecimal monto;

        try {

            monto = new BigDecimal(
                    item.getMonto()
            );

        } catch (NumberFormatException e) {

            throw new CuentaAnualInvalidaException(
                    "Monto inválido: "
                            + item.getMonto()
            );
        }


        // El repositorio indica que
        // montos negativos o cero son anomalías.

        // Monto cero no es válido
if (monto.compareTo(BigDecimal.ZERO) == 0) {
    throw new CuentaAnualInvalidaException(
            "Monto no puede ser cero"
    );
}

// Para retiros, compras y pagos normalizamos el monto a positivo
if (transaccion.equals("retiro")
        || transaccion.equals("compra")
        || transaccion.equals("pago")) {

    monto = monto.abs();
}

// Un depósito no puede venir negativo
if (transaccion.equals("deposito")
        && monto.compareTo(BigDecimal.ZERO) < 0) {

    throw new CuentaAnualInvalidaException(
            "Depósito con monto negativo: " + monto
    );
}

        cuenta.setMonto(monto);


        // =========================
        // DESCRIPCIÓN
        // =========================

        String descripcion =
                item.getDescripcion();

        if (descripcion == null ||
                descripcion.isBlank()) {

            descripcion =
                    "Sin descripción";
        }

        cuenta.setDescripcion(
                descripcion.trim()
        );


        return cuenta;
    }


    // =============================
    // CONVERTIR FECHA
    // =============================

    private LocalDate convertirFecha(
            String fecha) {

        if (fecha == null ||
                fecha.isBlank()) {

            throw new CuentaAnualInvalidaException(
                    "Fecha vacía"
            );
        }


        // yyyy-MM-dd
        try {

            return LocalDate.parse(
                    fecha,
                    DateTimeFormatter
                            .ofPattern("yyyy-MM-dd")
            );

        } catch (DateTimeParseException ignored) {
        }


        // yyyy/MM/dd
        try {

            return LocalDate.parse(
                    fecha,
                    DateTimeFormatter
                            .ofPattern("yyyy/MM/dd")
            );

        } catch (DateTimeParseException ignored) {
        }


        // dd/MM/yyyy
        try {

            return LocalDate.parse(
                    fecha,
                    DateTimeFormatter
                            .ofPattern("dd/MM/yyyy")
            );

        } catch (DateTimeParseException ignored) {
        }


        // dd-MM-yyyy
        try {

            return LocalDate.parse(
                    fecha,
                    DateTimeFormatter
                            .ofPattern("dd-MM-yyyy")
            );

        } catch (DateTimeParseException ignored) {
        }


        throw new CuentaAnualInvalidaException(
                "Formato de fecha inválido: "
                        + fecha
        );
    }
}
