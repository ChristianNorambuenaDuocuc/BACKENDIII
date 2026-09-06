package cl.duoc.bffatm.service;


import cl.duoc.bffatm.client.CuentaClient;
import cl.duoc.bffatm.client.InteresClient;
import cl.duoc.bffatm.dto.*;
import cl.duoc.bffatm.mapper.AtmMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BffAtmService {

    private final CuentaClient cuentaClient;
    private final InteresClient interesClient;

    public BffAtmService(
            CuentaClient cuentaClient,
            InteresClient interesClient) {

        this.cuentaClient = cuentaClient;
        this.interesClient = interesClient;
    }


    public SaldoAtmDTO consultarSaldo(Integer cuentaId) {

        List<InteresDTO> intereses =
                interesClient.obtenerPorCuentaId(cuentaId);

        if (intereses.isEmpty()) {
            return null;
        }

        InteresDTO cuenta = intereses.get(0);

        return AtmMapper.toSaldoDTO(
                cuentaId,
                cuenta
        );
    }


    public List<MovimientoAtmDTO> consultarMovimientos(
            Integer cuentaId) {

        List<CuentaDTO> cuentas =
                cuentaClient.obtenerPorCuentaId(cuentaId);

        return AtmMapper.toMovimientosDTO(cuentas);
    }


    public RetiroResponseDTO validarRetiro(
            Integer cuentaId,
            BigDecimal monto) {

        List<InteresDTO> intereses =
                interesClient.obtenerPorCuentaId(cuentaId);

        if (intereses.isEmpty()) {

            return new RetiroResponseDTO(
                    cuentaId,
                    monto,
                    false,
                    "Cuenta no encontrada"
            );
        }

        BigDecimal saldo =
                intereses.get(0).getSaldo();


        if (monto == null ||
                monto.compareTo(BigDecimal.ZERO) <= 0) {

            return new RetiroResponseDTO(
                    cuentaId,
                    monto,
                    false,
                    "Monto de retiro inválido"
            );
        }


        if (saldo == null) {

            return new RetiroResponseDTO(
                    cuentaId,
                    monto,
                    false,
                    "Saldo no disponible"
            );
        }


        if (monto.compareTo(saldo) > 0) {

            return new RetiroResponseDTO(
                    cuentaId,
                    monto,
                    false,
                    "Saldo insuficiente"
            );
        }


        return new RetiroResponseDTO(
                cuentaId,
                monto,
                true,
                "Retiro autorizado"
        );
    }
}