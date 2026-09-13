package cl.duoc.bffatm.models;


public record LoginResponse(
        String token,
        String tipo) {
}