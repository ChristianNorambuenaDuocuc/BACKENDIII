package cl.duoc.bffatm.mapper;

import cl.duoc.bffatm.dto.CuentaDTO;
import cl.duoc.bffatm.dto.InteresDTO;
import cl.duoc.bffatm.dto.MovimientoAtmDTO;
import cl.duoc.bffatm.dto.SaldoAtmDTO;

import java.util.List;

public class AtmMapper {

    private AtmMapper() {
    }

    public static SaldoAtmDTO toSaldoDTO(
            Integer cuentaId,
            InteresDTO interes) {

        return new SaldoAtmDTO(
                cuentaId,
                interes.getSaldo(),
                interes.getTipo()
        );
    }


    public static List<MovimientoAtmDTO> toMovimientosDTO(
            List<CuentaDTO> cuentas) {

        return cuentas.stream()
                .limit(5)
                .map(cuenta ->
                        new MovimientoAtmDTO(
                                cuenta.getFecha(),
                                cuenta.getTransaccion(),
                                cuenta.getMonto()
                        )
                )
                .toList();
    }
}