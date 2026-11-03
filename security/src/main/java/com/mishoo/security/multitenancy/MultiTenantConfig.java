package com.mishoo.security.multitenancy;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Slf4j
@Configuration
public class MultiTenantConfig {

    @Value("${spring.datasource.url}")
    private String defaultUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private static final String BASE_DB_NAME = "securitydb";

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSource defaultDs = createDataSourceForTenant(TenantContext.DEFAULT_TENANT);

        createDataSourceForTenant("norte");
        createDataSourceForTenant("sur");

        TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource(
                this::createDataSourceForTenant,
                defaultDs
        );
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    public DataSource createDataSourceForTenant(String tenant) {
        String cleanTenant = TenantContext.sanitizeTenant(tenant);
        String tenantDbName = BASE_DB_NAME + "_" + cleanTenant;
        String tenantUrl = buildTenantUrl(defaultUrl, tenantDbName);

        log.info("Configuring security tenant database: {} with URL: {}", tenantDbName, tenantUrl);

        try {
            ensureDatabaseExists(defaultUrl, tenantDbName, username, password);
            initSchemaAndSeed(tenantUrl, cleanTenant, username, password);
        } catch (Exception e) {
            log.warn("Database/schema auto-provisioning for security tenant {} encountered: {}", cleanTenant, e.getMessage());
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl(tenantUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(1);
        ds.setIdleTimeout(30000);
        ds.setConnectionTimeout(10000);
        ds.setPoolName("HikariPool-Security-" + cleanTenant);
        return ds;
    }

    private String buildTenantUrl(String baseUrl, String tenantDbName) {
        try {
            String clean = baseUrl.replace("jdbc:postgresql://", "http://");
            URI uri = new URI(clean);
            String userInfo = uri.getUserInfo() != null ? uri.getUserInfo() + "@" : "";
            int port = uri.getPort() != -1 ? uri.getPort() : 5432;
            String query = uri.getQuery() != null ? "?" + uri.getQuery() : "";
            return "jdbc:postgresql://" + userInfo + uri.getHost() + ":" + port + "/" + tenantDbName + query;
        } catch (Exception e) {
            int lastSlash = baseUrl.lastIndexOf('/');
            int questionMark = baseUrl.indexOf('?', lastSlash);
            String base = (questionMark != -1) ? baseUrl.substring(0, questionMark) : baseUrl;
            String query = (questionMark != -1) ? baseUrl.substring(questionMark) : "";
            int slashIndex = base.lastIndexOf('/');
            return base.substring(0, slashIndex + 1) + tenantDbName + query;
        }
    }

    private void ensureDatabaseExists(String baseUrl, String tenantDbName, String user, String pass) {
        String adminUrl = buildTenantUrl(baseUrl, "postgres");
        try (Connection conn = DriverManager.getConnection(adminUrl, user, pass);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + tenantDbName + "'");
            if (!rs.next()) {
                log.info("Creating security tenant database: {}", tenantDbName);
                stmt.execute("CREATE DATABASE \"" + tenantDbName + "\"");
            }
        } catch (Exception e) {
            log.debug("Notice while checking/creating database {}: {}", tenantDbName, e.getMessage());
        }
    }

    private void initSchemaAndSeed(String tenantUrl, String tenant, String user, String pass) {
        try (Connection conn = DriverManager.getConnection(tenantUrl, user, pass);
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGSERIAL PRIMARY KEY,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP
                );
            """);

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                String adminHash = encoder.encode("admin123");
                String centroHash = encoder.encode("centro123");
                String norteHash = encoder.encode("norte123");
                String mishooHash = encoder.encode("adminpassword123");

                stmt.execute(String.format("""
                    INSERT INTO users (email, password, role, created_at, updated_at) VALUES
                    ('admin@mishoo.com', '%s', 'ADMIN', NOW(), NOW()),
                    ('admin@barber.com', '%s', 'ADMIN', NOW(), NOW()),
                    ('admin@centro.com', '%s', 'ADMIN', NOW(), NOW()),
                    ('admin@norte.com', '%s', 'ADMIN', NOW(), NOW())
                    ON CONFLICT (email) DO NOTHING;
                """, mishooHash, adminHash, centroHash, norteHash));
            }

        } catch (Exception e) {
            log.debug("Notice while initializing schema for security tenant {}: {}", tenant, e.getMessage());
        }
    }
}
