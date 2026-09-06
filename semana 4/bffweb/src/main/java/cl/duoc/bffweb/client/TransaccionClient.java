package cl.duoc.bffweb.client;


import cl.duoc.bffweb.dto.TransaccionDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TransaccionClient {

    private final RestClient restClient;

    public TransaccionClient(
            RestClient.Builder builder,
            @Value("${services.transacciones.url}") String url) {

        this.restClient = builder
                .baseUrl(url)
                .build();
    }

    public List<TransaccionDTO> obtenerTodas() {

        return restClient
                .get()
                .uri("/api/transacciones")
                .retrieve()
                .body(
                    new ParameterizedTypeReference<List<TransaccionDTO>>() {
                    }
                );
    }
}
