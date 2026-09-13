package cl.duoc.bffatm.client;


import cl.duoc.bffatm.dto.InteresDTO;
import cl.duoc.bffatm.security.JwtTokenUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Component;

import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class InteresClient {

    private final RestClient restClient;
    private final JwtTokenUtil jwtTokenUtil;

    public InteresClient(
            RestClient.Builder builder,
            @Value("${services.intereses.url}") String url,
            JwtTokenUtil jwtTokenUtil) {

        this.restClient =
                builder
                        .baseUrl(url)
                        .build();

        this.jwtTokenUtil =
                jwtTokenUtil;
    }

    public List<InteresDTO> obtenerPorCuentaId(
            Integer cuentaId) {

        String serviceToken =
                generateServiceToken();

        return restClient
                .get()

                .uri(
                        "/api/intereses/cuenta/{cuentaId}",
                        cuentaId
                )

                .header(
                        "Authorization",
                        "Bearer " + serviceToken
                )

                .retrieve()

                .body(
                        new ParameterizedTypeReference<List<InteresDTO>>() {
                        }
                );
    }

    private String generateServiceToken() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        UserDetails userDetails =
                (UserDetails)
                        authentication.getPrincipal();

        return jwtTokenUtil
                .generateServiceToken(userDetails);
    }
}