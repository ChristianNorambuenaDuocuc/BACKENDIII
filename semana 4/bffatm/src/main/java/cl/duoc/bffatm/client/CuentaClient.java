package cl.duoc.bffatm.client;

import cl.duoc.bffatm.dto.CuentaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CuentaClient {

    private final RestClient restClient;

    public CuentaClient(
            RestClient.Builder builder,
            @Value("${services.cuentas.url}") String url) {

        this.restClient = builder
                .baseUrl(url)
                .build();
    }

    public List<CuentaDTO> obtenerPorCuentaId(Integer cuentaId) {

        return restClient
                .get()
                .uri(
                        "/api/cuentas/{cuentaId}",
                        cuentaId
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<List<CuentaDTO>>() {
                        }
                );
    }
}