package cl.duoc.interesesservice.security;


import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ServiceTokenFilter extends OncePerRequestFilter {

    private final ServiceTokenUtil serviceTokenUtil;

    public ServiceTokenFilter(
            ServiceTokenUtil serviceTokenUtil) {

        this.serviceTokenUtil = serviceTokenUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authorization header missing or invalid"
            );

            return;
        }

        try {

            String token =
                    authHeader.substring(7);

            Claims claims =
                    serviceTokenUtil.parseToken(token);

            String tipo =
                    claims.get(
                            "tipo",
                            String.class
                    );

            if (!"SERVICE_TOKEN".equals(tipo)) {

                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Token is not a SERVICE_TOKEN"
                );

                return;
            }

            request.setAttribute(
                    "claims",
                    claims
            );

        } catch (Exception e) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired service token"
            );

            return;
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}