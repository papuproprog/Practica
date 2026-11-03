package com.mishoo.security.schemas;

public record AuthResponse(
        String token,
        String email,
        String role,
        String branch,
        String tokenType
) {

    public static AuthResponse of(String token, String email, String role, String branch) {
        return new AuthResponse(token, email, role, branch, "Bearer");
    }

}
