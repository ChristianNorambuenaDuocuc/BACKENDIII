package cl.duoc.semana3sumativa.listener;

import cl.duoc.semana3sumativa.dto.InteresCsv;
import cl.duoc.semana3sumativa.model.Intereses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class InteresesSkipListener
        implements SkipListener<InteresCsv, Intereses> {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    InteresesSkipListener.class
            );

    @Override
    public void onSkipInRead(Throwable t) {

        logger.error(
                "Error leyendo intereses: {}",
                t.getMessage()
        );
    }

    @Override
    public void onSkipInProcess(
            InteresCsv item,
            Throwable t) {

        logger.warn(
                "Interés omitido - cuenta: {}, nombre: {}, saldo: {}, edad: {}, tipo: {}. Motivo: {}",
                item.getCuentaId(),
                item.getNombre(),
                item.getSaldo(),
                item.getEdad(),
                item.getTipo(),
                t.getMessage()
        );
    }

    @Override
    public void onSkipInWrite(
            Intereses item,
            Throwable t) {

        logger.error(
                "Error escribiendo cuenta {}. Motivo: {}",
                item.getCuentaId(),
                t.getMessage()
        );
    }
}
