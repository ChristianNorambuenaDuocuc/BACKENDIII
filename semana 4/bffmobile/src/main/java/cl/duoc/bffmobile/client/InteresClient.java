package cl.duoc.bffmobile.client;


import cl.duoc.bffmobile.dto.InteresDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class InteresClient {

    private final RestClient restClient;

    public InteresClient(
            RestClient.Builder builder,
            @Value("${services.intereses.url}") String url) {

        this.restClient = builder
                .baseUrl(url)
                .build();
    }

    public List<InteresDTO> obtenerPorCuentaId(Integer cuentaId) {

        return restClient
                .get()
                .uri(
                    "/api/intereses/cuenta/{cuentaId}",
                    cuentaId
                )
                .retrieve()
                .body(
                    new ParameterizedTypeReference<List<InteresDTO>>() {
                    }
                );
    }
}
