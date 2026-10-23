package com.mishoo.security.schemas;

import com.mishoo.security.models.User;

public record UserResponse(
        Long id,
        String email,
        String role
) {

    public static UserResponse fromEntity(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole());
    }

}
