package cl.duoc.bffweb.client;


import cl.duoc.bffweb.dto.TransaccionDTO;
import cl.duoc.bffweb.security.JwtTokenUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TransaccionClient {

    private final RestClient restClient;
    private final JwtTokenUtil jwtTokenUtil;

    public TransaccionClient(
            RestClient.Builder builder,
            @Value("${services.transacciones.url}") String url,
            JwtTokenUtil jwtTokenUtil) {

        this.restClient = builder
                .baseUrl(url)
                .build();

        this.jwtTokenUtil = jwtTokenUtil;
    }

    public List<TransaccionDTO> obtenerTodas() {

        String serviceToken = generateServiceToken();

        return restClient
                .get()
                .uri("/api/transacciones")
                .header(
                        "Authorization",
                        "Bearer " + serviceToken
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<List<TransaccionDTO>>() {
                        }
                );
    }

    private String generateServiceToken() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        return jwtTokenUtil.generateServiceToken(userDetails);
    }
}