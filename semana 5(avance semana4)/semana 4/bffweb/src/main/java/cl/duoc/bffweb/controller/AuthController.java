package cl.duoc.bffweb.controller;

import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bffweb.models.LoginRequest;
import cl.duoc.bffweb.models.LoginResponse;

import cl.duoc.bffweb.security.JwtTokenUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtTokenUtil jwtTokenUtil) {

        this.authenticationManager =
                authenticationManager;

        this.userDetailsService =
                userDetailsService;

        this.jwtTokenUtil =
                jwtTokenUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        // 1. Validamos usuario y password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        // 2. Recuperamos el usuario
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(
                        request.username()
                );

        // 3. Generamos JWT
        String token =
                jwtTokenUtil.generateToken(
                        userDetails
                );

        // 4. Respondemos al cliente
        return ResponseEntity.ok(
                new LoginResponse(
                        token,
                        "Bearer"
                )
        );
    }
}