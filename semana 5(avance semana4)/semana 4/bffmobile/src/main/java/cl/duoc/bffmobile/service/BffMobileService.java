package cl.duoc.bffmobile.service;


import cl.duoc.bffmobile.client.CuentaClient;
import cl.duoc.bffmobile.client.InteresClient;
import cl.duoc.bffmobile.dto.CuentaDTO;
import cl.duoc.bffmobile.dto.InteresDTO;
import cl.duoc.bffmobile.dto.ResumenMobileDTO;
import cl.duoc.bffmobile.mapper.ResumenMobileMapper;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class BffMobileService {

    private final CuentaClient cuentaClient;
    private final InteresClient interesClient;


    public BffMobileService(
            CuentaClient cuentaClient,
            InteresClient interesClient) {

        this.cuentaClient = cuentaClient;
        this.interesClient = interesClient;
    }


    public ResumenMobileDTO obtenerResumen(Integer cuentaId) {

        List<CuentaDTO> cuentas =
                cuentaClient.obtenerPorCuentaId(cuentaId);

        List<InteresDTO> intereses =
                interesClient.obtenerPorCuentaId(cuentaId);


        return ResumenMobileMapper.toDTO(
                cuentaId,
                cuentas,
                intereses
        );
    }
}