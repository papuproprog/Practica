package com.mishoo.security.schemas;

public record AuthResponse(
        String token,
        String email,
        String role,
        String tokenType
) {

    public static AuthResponse of(String token, String email, String role) {
        return new AuthResponse(token, email, role, "Bearer");
    }

}
