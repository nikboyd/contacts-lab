package dev.educery.storage;

import dev.educery.domain.MailAddress;
import dev.educery.storage.Hashed.Search;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * A storage mechanism for mail addresses.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface AddressStorage
        extends CrudRepository<MailAddress, Long>, Search<MailAddress> {

    @Query("SELECT m FROM MailAddress m WHERE m.key = :key")
    MailAddress findKey(@Param("key") Long key);

    @Override
    @Query("SELECT m FROM MailAddress m WHERE m.hashKey = :hashKey")
    MailAddress findHash(@Param("hashKey") Long key);

} // AddressStorage
