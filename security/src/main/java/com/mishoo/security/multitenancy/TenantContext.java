package com.mishoo.security.multitenancy;

public class TenantContext {

    public static final String DEFAULT_TENANT = "centro";
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setCurrentTenant(String tenant) {
        if (tenant == null || tenant.isBlank()) {
            CURRENT_TENANT.set(DEFAULT_TENANT);
        } else {
            CURRENT_TENANT.set(sanitizeTenant(tenant));
        }
    }

    public static String getCurrentTenant() {
        String tenant = CURRENT_TENANT.get();
        return (tenant != null && !tenant.isBlank()) ? tenant : DEFAULT_TENANT;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    public static String sanitizeTenant(String tenant) {
        if (tenant == null) return DEFAULT_TENANT;
        String sanitized = tenant.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "");
        return sanitized.isEmpty() ? DEFAULT_TENANT : sanitized;
    }
}
