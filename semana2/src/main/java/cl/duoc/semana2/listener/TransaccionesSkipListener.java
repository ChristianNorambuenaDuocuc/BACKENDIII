package cl.duoc.semana2.listener;

import cl.duoc.semana2.dto.TransaccionCsv;
import cl.duoc.semana2.model.Transacciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class TransaccionesSkipListener
        implements SkipListener<TransaccionCsv, Transacciones> {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    TransaccionesSkipListener.class
            );

    @Override
    public void onSkipInRead(Throwable t) {

        logger.error(
                "Error leyendo transacciones: {}",
                t.getMessage()
        );
    }

    @Override
    public void onSkipInProcess(
            TransaccionCsv item,
            Throwable t) {

        logger.warn(
                "Transacción omitida - id: {}, fecha: {}, monto: {}, tipo: {}. Motivo: {}",
                item.getId(),
                item.getFecha(),
                item.getMonto(),
                item.getTipo(),
                t.getMessage()
        );
    }

    @Override
    public void onSkipInWrite(
            Transacciones item,
            Throwable t) {

        logger.error(
                "Error escribiendo transacción id {}. Motivo: {}",
                item.getId(),
                t.getMessage()
        );
    }
}
