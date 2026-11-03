package com.mishoo.barbers.multitenancy;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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

    private static final String BASE_DB_NAME = "barberdb";

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSource defaultDs = createDataSourceForTenant(TenantContext.DEFAULT_TENANT);

        // Pre-initialize standard branches
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

        log.info("Configuring tenant database: {} with URL: {}", tenantDbName, tenantUrl);

        try {
            ensureDatabaseExists(defaultUrl, tenantDbName, username, password);
            initSchemaAndSeed(tenantUrl, cleanTenant, username, password);
        } catch (Exception e) {
            log.warn("Database/schema auto-provisioning for tenant {} encountered: {}", cleanTenant, e.getMessage());
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
        ds.setPoolName("HikariPool-Barbers-" + cleanTenant);
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
                log.info("Creating tenant database: {}", tenantDbName);
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
                CREATE TABLE IF NOT EXISTS barbers (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    last_name VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP
                );
                CREATE TABLE IF NOT EXISTS services (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    description TEXT,
                    duration_minutes INTEGER NOT NULL,
                    price DOUBLE PRECISION NOT NULL,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP
                );
            """);

            // Seed services if table is empty
            ResultSet rsServices = stmt.executeQuery("SELECT COUNT(*) FROM services");
            if (rsServices.next() && rsServices.getInt(1) == 0) {
                stmt.execute("""
                    INSERT INTO services (name, description, duration_minutes, price, created_at, updated_at) VALUES
                    ('Corte de cabello', 'Corte clásico o moderno con tijera y máquina, incluye lavado rápido', 30, 180.0, NOW(), NOW()),
                    ('Arreglo de barba', 'Perfilado, recorte a tijera y máquina con hidratación de aceites', 20, 120.0, NOW(), NOW()),
                    ('Corte y barba', 'Combo completo: corte de cabello personalizado y diseño de barba', 50, 260.0, NOW(), NOW()),
                    ('Afeitado tradicional', 'Afeitado al ras con navaja, toallas calientes y loción refrescante', 30, 150.0, NOW(), NOW()),
                    ('Tinte / Coloración', 'Aplicación de tinte capilar o matizador de canas profesional', 60, 350.0, NOW(), NOW()),
                    ('Tratamiento facial', 'Limpieza profunda, mascarilla de carbón y exfoliación', 40, 250.0, NOW(), NOW()),
                    ('Perfilado de cejas', 'Limpieza y diseño de cejas masculinas con navaja', 15, 80.0, NOW(), NOW()),
                    ('Lavado y peinado', 'Lavado con masaje capilar relajante y peinado con pomada mate', 25, 100.0, NOW(), NOW());
                """);
            }

            // Seed initial barbers according to branch
            ResultSet rsBarbers = stmt.executeQuery("SELECT COUNT(*) FROM barbers");
            if (rsBarbers.next() && rsBarbers.getInt(1) == 0) {
                if ("norte".equals(tenant)) {
                    stmt.execute("""
                        INSERT INTO barbers (name, last_name, created_at, updated_at) VALUES
                        ('Alejandro', 'Ruiz', NOW(), NOW()),
                        ('Javier', 'López', NOW(), NOW());
                    """);
                } else if ("sur".equals(tenant)) {
                    stmt.execute("""
                        INSERT INTO barbers (name, last_name, created_at, updated_at) VALUES
                        ('Mateo', 'Vargas', NOW(), NOW()),
                        ('Gabriel', 'Cruz', NOW(), NOW());
                    """);
                } else {
                    stmt.execute("""
                        INSERT INTO barbers (name, last_name, created_at, updated_at) VALUES
                        ('Carlos', 'Mendoza', NOW(), NOW()),
                        ('Daniel', 'Soto', NOW(), NOW());
                    """);
                }
            }

        } catch (Exception e) {
            log.debug("Notice while initializing schema for {}: {}", tenant, e.getMessage());
        }
    }
}
