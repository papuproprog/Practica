package com.mishoo.security.schemas;

public record TokenValidationResponse(
        boolean valid,
        String email,
        String role
) {

    public static TokenValidationResponse valid(String email, String role) {
        return new TokenValidationResponse(true, email, role);
    }

    public static TokenValidationResponse invalid() {
        return new TokenValidationResponse(false, null, null);
    }

}
