package cl.duoc.bffatm.models;


public record LoginRequest(
        String username,
        String password) {
}