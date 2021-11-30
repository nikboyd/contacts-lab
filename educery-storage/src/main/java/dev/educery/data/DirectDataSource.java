package dev.educery.data;

import javax.sql.DataSource;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import dev.educery.utils.Logging;

/**
 * Configures a data source from a properties file.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@Configuration
@Profile("direct")
@PropertySource(DirectDataSource.DatabaseConfiguration)
public class DirectDataSource extends BasicDataSource implements Logging {

    public static final String DatabaseConfiguration = "classpath:db.properties";

    @Value("${${db.type}.db.username}") private String databaseUsername;
    @Value("${${db.type}.db.password}") private String databasePassword;
    @Value("${${db.type}.db.driver}") private String driverClassName;
    @Value("${${db.type}.db.url}") private String databaseURL;

    static final String DirectURL = "JPA direct URL: %s";
    static final String DirectDriver = "JPA direct driver: %s";
    @Bean public DataSource dataSource() {
        report(String.format(DirectDriver, driverClassName));
        report(String.format(DirectURL, databaseURL));

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUsername(databaseUsername);
        dataSource.setPassword(databasePassword);
        dataSource.setUrl(databaseURL);
        return dataSource;
    }

} // DirectDataSource
