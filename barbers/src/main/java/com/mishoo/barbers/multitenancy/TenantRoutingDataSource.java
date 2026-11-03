package com.mishoo.barbers.multitenancy;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<Object, Object> targetDataSources = new ConcurrentHashMap<>();
    private final Function<String, DataSource> dataSourceProvider;
    private DataSource defaultDataSource;

    public TenantRoutingDataSource(Function<String, DataSource> dataSourceProvider, DataSource defaultDataSource) {
        this.dataSourceProvider = dataSourceProvider;
        this.defaultDataSource = defaultDataSource;
        this.targetDataSources.put(TenantContext.DEFAULT_TENANT, defaultDataSource);
        super.setTargetDataSources(this.targetDataSources);
        super.setDefaultTargetDataSource(defaultDataSource);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String tenant = TenantContext.getCurrentTenant();
        Object existing = targetDataSources.get(tenant);
        if (existing instanceof DataSource ds) {
            return ds;
        }

        DataSource newDs = dataSourceProvider.apply(tenant);
        if (newDs != null) {
            targetDataSources.put(tenant, newDs);
            super.setTargetDataSources(targetDataSources);
            super.afterPropertiesSet();
            return newDs;
        }

        return defaultDataSource;
    }
}
