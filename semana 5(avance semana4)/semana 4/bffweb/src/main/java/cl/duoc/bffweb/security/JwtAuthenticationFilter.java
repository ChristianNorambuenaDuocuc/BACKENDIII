package cl.duoc.bffweb.security;


import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenUtil jwtTokenUtil,
            UserDetailsService userDetailsService) {

        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        System.out.println("================================");
        System.out.println("JWT FILTER");
        System.out.println("Ruta: " + request.getRequestURI());
        System.out.println("Authorization recibido: " + authHeader);
        System.out.println("================================");

        // Si no viene JWT, dejamos continuar.
        // SecurityConfig decidirá si la ruta necesita autenticación.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            System.out.println("No viene Bearer Token");

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            String username =
                    jwtTokenUtil.getUsernameFromToken(token);

            System.out.println("Usuario del token: " + username);

            if (username != null
                    && SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(username);

                System.out.println(
                        "Authorities del usuario: "
                                + userDetails.getAuthorities()
                );

                boolean tokenValido =
                        jwtTokenUtil.validateToken(
                                token,
                                userDetails
                        );

                System.out.println(
                        "Token valido: " + tokenValido
                );

                if (tokenValido) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);

                    System.out.println(
                            "Usuario autenticado correctamente"
                    );

                    System.out.println(
                            "Authentication: "
                                    + SecurityContextHolder
                                            .getContext()
                                            .getAuthentication()
                    );

                    System.out.println(
                            "Roles finales: "
                                    + SecurityContextHolder
                                            .getContext()
                                            .getAuthentication()
                                            .getAuthorities()
                    );
                }
            }

        } catch (Exception e) {

            System.out.println("ERROR AL VALIDAR JWT");
            System.out.println(
                    e.getClass().getSimpleName()
                            + ": "
                            + e.getMessage()
            );

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token JWT invalido o expirado"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}