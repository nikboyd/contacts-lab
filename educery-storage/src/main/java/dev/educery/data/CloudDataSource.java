package dev.educery.data;

import javax.sql.DataSource;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import dev.educery.utils.Logging;
import org.apache.commons.lang3.StringUtils;

/**
 * Configures a data source from environment variables.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@Configuration
@Profile("cloud")
@PropertySource(CloudDataSource.DatabaseConfiguration)
public class CloudDataSource extends BasicDataSource implements Logging {

    static final String DatabaseConfiguration = "classpath:db.properties";

    @Value("#{environment['${cloud.db.username}']}") String databaseUsername;
    @Value("#{environment['${cloud.db.password}']}") String databasePassword;

    @Value("#{environment['${cloud.db.host}']}") String databaseHost;
    private String databaseHost() { return StringUtils.defaultString(databaseHost, "localhost"); }

    @Value("#{environment['${cloud.db.name}']}") String databaseName;
    private String databaseName() { return StringUtils.defaultString(databaseName, "samples"); }

    @Value("${cloud.db.driver}") String driverClassName;
    @Value("${cloud.db.url.pattern}") String cloudUrlPattern;
    private String formURL() { return String.format(cloudUrlPattern, databaseHost(), databaseName()); }

    static final String CloudURL = "JPA cloud URL: %s";
    static final String CloudDriver = "JPA cloud driver: %s";
    @Bean public DataSource dataSource() {
        String databaseURL = formURL();
        report(String.format(CloudDriver, driverClassName));
        report(String.format(CloudURL, databaseURL));

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUsername(databaseUsername);
        dataSource.setPassword(databasePassword);
        dataSource.setUrl(databaseURL);
        return dataSource;
    }

} // CloudDataSource
