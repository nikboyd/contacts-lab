package dev.educery.storage;

import javax.persistence.EntityManagerFactory;

import org.springframework.orm.jpa.*;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import dev.educery.data.*;
import dev.educery.domain.*;

/**
 * Configures the persistence mechanisms.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@Configuration
@EnableTransactionManagement
@Import({ CloudDataSource.class, DirectDataSource.class })
@EnableJpaRepositories(basePackages = { PersistenceContext.StoragePackage })
public class PersistenceContext {

    static final String StoragePackage = "dev.educery.storage"; // this package

    @Autowired(required = false)
    CloudDataSource cloudDataSource;

    @Profile("cloud")
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean cloudEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setDataSource(cloudDataSource.dataSource());
        em.setPackagesToScan(cloudDataSource.modelPackages());
        em.setJpaProperties(cloudDataSource.additionalProperties());
        return em;
    }


    @Autowired(required = false)
    DirectDataSource directDataSource;

    @Profile("direct")
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean directEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setDataSource(directDataSource.dataSource());
        em.setPackagesToScan(directDataSource.modelPackages());
        em.setJpaProperties(directDataSource.additionalProperties());
        return em;
    }

    @Bean public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean public JpaRepositoryFactory repositoryFactory(EntityManagerFactory emf) {
        return new JpaRepositoryFactory(emf.createEntityManager()); }


    @Bean public StorageMechanism<Contact, ContactStorage> contactStorageMechanism(ContactStorage store) {
        return new StorageMechanism(store, ContactStorage.class, Contact.class); }

    @Bean public StorageMechanism<PhoneNumber, PhoneStorage> phoneStorageMechanism(PhoneStorage store) {
        return new StorageMechanism(store, PhoneStorage.class, PhoneNumber.class); }

    @Bean public StorageMechanism<EmailAddress, EmailStorage> emailStorageMechanism(EmailStorage store) {
        return new StorageMechanism(store, EmailStorage.class, EmailAddress.class); }

    @Bean public StorageMechanism<MailAddress, AddressStorage> addressStorageMechanism(AddressStorage store) {
        return new StorageMechanism(store, AddressStorage.class, MailAddress.class); }

    @Bean public StorageMechanism.Registry storageRegistry(
            StorageMechanism<Contact, ContactStorage> contactStorage,
            StorageMechanism<PhoneNumber, PhoneStorage> phoneStorage,
            StorageMechanism<EmailAddress, EmailStorage> emailStorage,
            StorageMechanism<MailAddress, AddressStorage> addressStorage) {
        return StorageMechanism.Registry.with(
            phoneStorage, emailStorage, addressStorage, contactStorage); }

} // PersistenceContext
