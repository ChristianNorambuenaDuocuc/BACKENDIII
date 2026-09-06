package cl.duoc.bffweb.mapper;


import cl.duoc.bffweb.dto.*;

import java.util.List;

public class ResumenWebMapper {

    private ResumenWebMapper() {
    }

    public static ResumenWebDTO toDTO(
            Integer cuentaId,
            List<CuentaDTO> cuentas,
            List<InteresDTO> intereses,
            List<TransaccionDTO> transacciones) {

        return new ResumenWebDTO(
                cuentaId,
                cuentas,
                intereses,
                transacciones
        );
    }
}
