package cl.duoc.bffatm.client;


import cl.duoc.bffatm.dto.CuentaDTO;
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
public class CuentaClient {

    private final RestClient restClient;
    private final JwtTokenUtil jwtTokenUtil;

    public CuentaClient(
            RestClient.Builder builder,
            @Value("${services.cuentas.url}") String url,
            JwtTokenUtil jwtTokenUtil) {

        this.restClient =
                builder
                        .baseUrl(url)
                        .build();

        this.jwtTokenUtil =
                jwtTokenUtil;
    }

    public List<CuentaDTO> obtenerPorCuentaId(
            Integer cuentaId) {

        String serviceToken =
                generateServiceToken();

        return restClient
                .get()

                .uri(
                        "/api/cuentas/{cuentaId}",
                        cuentaId
                )

                .header(
                        "Authorization",
                        "Bearer " + serviceToken
                )

                .retrieve()

                .body(
                        new ParameterizedTypeReference<List<CuentaDTO>>() {
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