package dev.educery.storage;

import dev.educery.domain.EmailAddress;
import dev.educery.storage.Hashed.Search;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * A storage mechanism for email addresses.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface EmailStorage
        extends CrudRepository<EmailAddress, Long>, Search<EmailAddress> {

    @Query("SELECT e FROM EmailAddress e WHERE e.key = :key")
    EmailAddress findKey(@Param("key") Long key);

    @Override
    @Query("SELECT e FROM EmailAddress e WHERE e.hashKey = :hashKey")
    EmailAddress findHash(@Param("hashKey") Long key);

} // EmailStorage
