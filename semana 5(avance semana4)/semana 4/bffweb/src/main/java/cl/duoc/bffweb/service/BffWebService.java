package cl.duoc.bffweb.service;


import cl.duoc.bffweb.client.CuentaClient;
import cl.duoc.bffweb.client.InteresClient;
import cl.duoc.bffweb.client.TransaccionClient;
import cl.duoc.bffweb.dto.*;
import cl.duoc.bffweb.mapper.ResumenWebMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BffWebService {

    private final CuentaClient cuentaClient;
    private final InteresClient interesClient;
    private final TransaccionClient transaccionClient;


    public BffWebService(
            CuentaClient cuentaClient,
            InteresClient interesClient,
            TransaccionClient transaccionClient) {

        this.cuentaClient = cuentaClient;
        this.interesClient = interesClient;
        this.transaccionClient = transaccionClient;
    }


    public ResumenWebDTO obtenerResumen(Integer cuentaId) {

        List<CuentaDTO> cuentas =
                cuentaClient.obtenerPorCuentaId(cuentaId);

        List<InteresDTO> intereses =
                interesClient.obtenerPorCuentaId(cuentaId);

        List<TransaccionDTO> transacciones =
                transaccionClient.obtenerTodas();


        return ResumenWebMapper.toDTO(
                cuentaId,
                cuentas,
                intereses,
                transacciones
        );
    }
}
