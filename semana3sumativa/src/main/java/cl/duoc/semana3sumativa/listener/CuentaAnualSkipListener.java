package cl.duoc.semana3sumativa.listener;

import cl.duoc.semana3sumativa.dto.CuentaAnualCsv;
import cl.duoc.semana3sumativa.model.CuentaAnual;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class CuentaAnualSkipListener
        implements SkipListener<CuentaAnualCsv, CuentaAnual> {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CuentaAnualSkipListener.class
            );

    @Override
    public void onSkipInRead(Throwable t) {

        logger.error(
                "Error leyendo cuenta anual: {}",
                t.getMessage()
        );
    }

    @Override
    public void onSkipInProcess(
            CuentaAnualCsv item,
            Throwable t) {

        logger.warn(
                "Cuenta anual omitida - cuenta: {}, fecha: {}, transacción: {}, monto: {}. Motivo: {}",
                item.getCuentaId(),
                item.getFecha(),
                item.getTransaccion(),
                item.getMonto(),
                t.getMessage()
        );
    }

    @Override
    public void onSkipInWrite(
            CuentaAnual item,
            Throwable t) {

        logger.error(
                "Error escribiendo cuenta anual {}. Motivo: {}",
                item.getCuentaId(),
                t.getMessage()
        );
    }
}
