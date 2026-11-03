package com.mishoo.security.schemas;

import com.mishoo.security.models.User;
import com.mishoo.security.multitenancy.TenantContext;

public record UserResponse(
        Long id,
        String email,
        String role,
        String branch
) {

    public static UserResponse fromEntity(User user) {
        String b = (user.getBranch() != null && !user.getBranch().isBlank())
                ? user.getBranch()
                : TenantContext.getCurrentTenant();
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), b);
    }

}
