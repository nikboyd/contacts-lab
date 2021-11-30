package dev.educery.data;

import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Abstract data source.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public abstract class BasicDataSource {

    public static final String Comma = ",";
    public static final String HibernateDialect = "hibernate.dialect";
    public static final String HibernateCodeDDL = "hibernate.hbm2ddl.auto";

    @Bean public static PropertySourcesPlaceholderConfigurer propertyReplacer() {
        return new PropertySourcesPlaceholderConfigurer(); }

    @Value("${${db.type}.db.dialect}") private String databaseDialect;
    @Value("${${db.type}.db.code.ddl}") private String codeGeneration;

    @Value("${db.model.packages}") private String modelPackages;
    public String[] modelPackages() { return modelPackages.split(Comma); }

    public Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty(HibernateDialect, databaseDialect);
        if (!codeGeneration.isEmpty()) {
            properties.setProperty(HibernateCodeDDL, codeGeneration);
        }
        return properties;
    }

}
