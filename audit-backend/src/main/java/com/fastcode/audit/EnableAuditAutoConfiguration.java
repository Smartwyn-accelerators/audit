package com.fastcode.audit;

import com.fastcode.audit.application.ApiAudit;
import org.audit4j.core.AuditManager;
import org.audit4j.core.IAuditManager;
import org.audit4j.core.handler.ConsoleAuditHandler;
import org.audit4j.core.handler.Handler;
import org.audit4j.core.handler.file.FileAuditHandler;
import org.audit4j.core.layout.CustomizableLayout;
import org.audit4j.core.layout.Layout;
import org.audit4j.core.layout.SecureLayout;
import org.audit4j.core.layout.SimpleLayout;
import org.audit4j.integration.spring.AuditAspect;
import org.audit4j.integration.spring.SpringAudit4jConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Sink;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(AuditLayoutProperties.class)
@ComponentScan("com.fastcode.audit")
@EntityScan(basePackages = "com.fastcode.audit.domain")
@EnableJpaRepositories(basePackages = "com.fastcode.audit.domain")
public class EnableAuditAutoConfiguration {

    @Autowired
    AuditPropertiesConfiguration env;


    @Bean
    public AuditAspect auditAspect() {
        return new AuditAspect();
    }

    @Bean
    public DataSource auditDataSource() {
        try {
            return DataSourceBuilder.create()
                    .url(env.getAuditDatabaseUrl())
                    .username(env.getAuditDatabaseUsername())
                    .password(env.getAuditDatabasePassword())
                    .build();
        } catch (Exception e) {
            return null;
        }

    }

    @Bean
    public CustomDatabaseAuditHandler databaseHandler() {
        CustomDatabaseAuditHandler dbHandler = new CustomDatabaseAuditHandler();
        dbHandler.setDb_connection_type("pooled");
        dbHandler.setDataSource(auditDataSource());
        dbHandler.setEmbedded("false");
        dbHandler.setSeparate(true);
        return dbHandler;
    }

    private Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("log.file.location", env.getAuditFilePath());
        properties.put("log.file.prefix", env.getAuditFilePrefix());
        return properties;
    }

    @Bean
    public FileAuditHandler fileAuditHandler() {
        FileAuditHandler fileAuditHandler = new FileAuditHandler();
        fileAuditHandler.setAuditFilePrefix(env.getAuditFilePrefix());
        fileAuditHandler.setPath(env.getAuditFilePath());
        return fileAuditHandler;
    }

    @Bean
    public SpringAudit4jConfig springAudit4jConfig() {
        SpringAudit4jConfig springAudit4jConfig = new SpringAudit4jConfig();

        List<Handler> handlers = getHandlers();

        springAudit4jConfig.setHandlers(handlers);
        springAudit4jConfig.setProperties(getProperties());
        springAudit4jConfig.setLayout(getLayout());

        return springAudit4jConfig;
    }

    private List<Handler> getHandlers() {
        List<Handler> handlers = new ArrayList<>();
        if (env.isAuditConsoleEnabled()) {
            handlers.add(new ConsoleAuditHandler());
        }
        if (env.isAuditDatabaseEnabled()) {
            handlers.add(databaseHandler());
        }
        if (env.isAuditFileEnabled()) {
            handlers.add(fileAuditHandler());
        }
        return handlers;
    }

    @Bean
    public IAuditManager auditManager() {
        return AuditManager.startWithConfiguration(getConfiguration());
    }

    private org.audit4j.core.Configuration getConfiguration() {
        org.audit4j.core.Configuration configuration = new org.audit4j.core.Configuration();
        List<Handler> handlers = getHandlers();

        configuration.setHandlers(handlers);
        configuration.setProperties(getProperties());
        configuration.setLayout(getLayout());

        return configuration;
    }

    private Layout getLayout() {

        if (env.isSecureLayoutEnabled()) {
            SecureLayout layout = new SecureLayout();
            layout.setKey(env.getSecureLayoutKey());
            layout.setSalt(env.getSecureLayoutSalt());
            return layout;
        }

        if (env.getAuditLayoutTemplate().isEmpty()) {
            return new SimpleLayout();
        }

        CustomizableLayout layout = new CustomizableLayout();
        layout.setTemplate(env.getAuditLayoutTemplate());
        return layout;
    }

    @Bean
    public Sink sink(final HttpLogFormatter formatter, final HttpLogWriter writer) {
        return new ApiAudit();
    }

}
