package cl.duoc.bffweb.models;


public record LoginRequest(
        String username,
        String password) {
}