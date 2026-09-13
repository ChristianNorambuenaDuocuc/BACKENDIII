package cl.duoc.bffweb.security;


import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import cl.duoc.bffweb.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenUtil {

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;
    private SecretKey serviceSecretKey;

    public JwtTokenUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {

        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );

        this.serviceSecretKey = Keys.hmacShaKeyFor(
                jwtProperties.getServiceSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    // ======================================================
    // TOKEN DEL USUARIO
    // Cliente -> BFF
    // ======================================================

    public String generateToken(UserDetails userDetails) {

        Date now = new Date();

        Date expiryDate = new Date(
                now.getTime() + jwtProperties.getExpiration()
        );

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // ======================================================
    // SERVICE TOKEN
    // BFF -> MICROSERVICIO
    // ======================================================

    public String generateServiceToken(UserDetails userDetails) {

        Date now = new Date();

        Date expiryDate = new Date(
                now.getTime() + jwtProperties.getServiceExpiration()
        );

        String rol = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_WEB_USER")
                .replace("ROLE_", "");

        return Jwts.builder()
                .issuer("bffweb")
                .subject(userDetails.getUsername())
                .claim("rol", rol)
                .claim(
                        "email",
                        userDetails.getUsername() + "@example.com"
                )
                .claim(
                        "nombre",
                        userDetails.getUsername()
                )
                .claim(
                        "tipo",
                        "SERVICE_TOKEN"
                )
                .audience()
                    .add("semana4")
                .and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(serviceSecretKey)
                .compact();
    }

    // ======================================================
    // LEER JWT DEL USUARIO
    // ======================================================

    public Claims getClaimsFromToken(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {

        return getClaimsFromToken(token)
                .getSubject();
    }

    public boolean validateToken(
            String token,
            UserDetails userDetails) {

        String username =
                getUsernameFromToken(token);

        return username.equals(
                userDetails.getUsername()
        );
    }
}