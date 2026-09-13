package cl.duoc.cuentasservice.mapper;

import cl.duoc.cuentasservice.dto.cuentasDTO;
import cl.duoc.cuentasservice.model.cuentas;

public class cuentasMapper {

    private cuentasMapper() {
    }

    public static cuentasDTO toDTO(cuentas cuentas) {

        return new cuentasDTO(
                cuentas.getcuentasId(),
                cuentas.getFecha(),
                cuentas.getTransaccion(),
                cuentas.getMonto(),
                cuentas.getDescripcion()
        );
    }

    public static cuentas toModel(cuentasDTO dto) {

        return new cuentas(
                dto.getcuentasId(),
                dto.getFecha(),
                dto.getTransaccion(),
                dto.getMonto(),
                dto.getDescripcion()
        );
    }
}