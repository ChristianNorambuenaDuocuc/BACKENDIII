package cl.duoc.bffmobile.mapper;


import cl.duoc.bffmobile.dto.*;

import java.util.List;

public class ResumenMobileMapper {

    private ResumenMobileMapper() {
    }

    public static ResumenMobileDTO toDTO(
            Integer cuentaId,
            List<CuentaDTO> cuentas,
            List<InteresDTO> intereses) {

        InteresDTO informacionCuenta =
                intereses.isEmpty()
                        ? null
                        : intereses.get(0);


        List<MovimientoMobileDTO> movimientos =
                cuentas.stream()
                        .limit(5)
                        .map(cuenta ->
                                new MovimientoMobileDTO(
                                        cuenta.getFecha(),
                                        cuenta.getTransaccion(),
                                        cuenta.getMonto()
                                )
                        )
                        .toList();


        return new ResumenMobileDTO(
                cuentaId,

                informacionCuenta != null
                        ? informacionCuenta.getNombre()
                        : null,

                informacionCuenta != null
                        ? informacionCuenta.getSaldo()
                        : null,

                informacionCuenta != null
                        ? informacionCuenta.getTipo()
                        : null,

                movimientos
        );
    }
}