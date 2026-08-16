package cl.duoc.semana1.processor;

import cl.duoc.semana1.dto.TransaccionCsv;
import cl.duoc.semana1.exception.TransaccionInvalidaException;
import cl.duoc.semana1.model.Transacciones;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Component
public class TransaccionesProcessor
        implements ItemProcessor<TransaccionCsv, Transacciones> {

    private final Set<Long> idsProcesados = new HashSet<>();

    @Override
    public Transacciones process(TransaccionCsv item) throws Exception {

        Transacciones transaccion = new Transacciones();

        // =========================
        // ID
        // =========================

        Long id;

        try {
            id = Long.parseLong(item.getId());
        } catch (NumberFormatException e) {
            throw new TransaccionInvalidaException(
                    "ID inválido: " + item.getId()
            );
        }

        // Verificar si ya fue procesado
        if (idsProcesados.contains(id)) {
            throw new TransaccionInvalidaException(
                    "Transacción duplicada. ID: " + id
            );
        }

        // =========================
        // FECHA
        // =========================

        LocalDate fecha = convertirFecha(item.getFecha());

        // =========================
        // MONTO
        // =========================

        BigDecimal monto = convertirMonto(item.getMonto());

        // =========================
        // TIPO
        // =========================

        String tipo = validarTipo(item.getTipo());

        // =========================
        // TODAS LAS VALIDACIONES OK
        // =========================

        transaccion.setId(id);
        transaccion.setFecha(fecha);
        transaccion.setMonto(monto);
        transaccion.setTipo(tipo);

        // Solo ahora lo marcamos como procesado
        idsProcesados.add(id);

        return transaccion;
    }


    private LocalDate convertirFecha(String fecha) {

        if (fecha == null || fecha.isBlank()) {
            throw new TransaccionInvalidaException(
                    "Fecha vacía"
            );
        }

        // yyyy-MM-dd
        try {
            return LocalDate.parse(
                    fecha,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            );
        } catch (Exception ignored) {
        }

        // yyyy/MM/dd
        try {
            return LocalDate.parse(
                    fecha,
                    DateTimeFormatter.ofPattern("yyyy/MM/dd")
            );
        } catch (Exception ignored) {
        }

        // dd/MM/yyyy
        try {
            return LocalDate.parse(
                    fecha,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );
        } catch (Exception ignored) {
        }

        throw new TransaccionInvalidaException(
                "Formato de fecha inválido: " + fecha
        );
    }


    private BigDecimal convertirMonto(String monto) {

        if (monto == null || monto.isBlank()) {
            throw new TransaccionInvalidaException(
                    "Monto vacío"
            );
        }

        BigDecimal montoConvertido;

        try {
            montoConvertido = new BigDecimal(monto);
        } catch (NumberFormatException e) {
            throw new TransaccionInvalidaException(
                    "Monto inválido: " + monto
            );
        }

        if (montoConvertido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransaccionInvalidaException(
                    "El monto debe ser mayor que cero: " + monto
            );
        }

        return montoConvertido;
    }


    private String validarTipo(String tipo) {

        if (tipo == null || tipo.isBlank()) {
            throw new TransaccionInvalidaException(
                    "Tipo de transacción vacío"
            );
        }

        String tipoNormalizado =
                tipo.trim().toLowerCase();

        if (!tipoNormalizado.equals("credito")
                && !tipoNormalizado.equals("debito")) {

            throw new TransaccionInvalidaException(
                    "Tipo de transacción inválido: " + tipo
            );
        }

        return tipoNormalizado;
    }
}