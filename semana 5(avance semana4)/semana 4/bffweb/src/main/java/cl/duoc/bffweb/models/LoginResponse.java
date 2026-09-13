package cl.duoc.bffweb.models;


public record LoginResponse(
        String token,
        String tipo) {
}