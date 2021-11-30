package dev.educery.storage;

import java.util.*;
import dev.educery.domain.Contact;
import dev.educery.domain.Contact.IContactSearch;
import dev.educery.storage.Hashed.Search;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * A storage mechanism for contacts.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface ContactStorage
        extends CrudRepository<Contact, Long>, IContactSearch {

    @Override
    @Query("SELECT c FROM Contact c WHERE c.key = :key")
    Contact findKey(@Param("key") Long key);

    @Override
    @Query("SELECT c FROM Contact c WHERE c.hashKey = :hashKey")
    Contact findHash(@Param("hashKey") Long key);

    @Override
    @Query("SELECT c FROM Contact c WHERE c.name like :sample order by c.name")
    List<Contact> findLike(@Param("sample") String sample);

    @Override
    @Query("SELECT c FROM Contact c join c.emails em WHERE em.hashKey = :emailKey")
    List<Contact> findEmail(@Param("emailKey") Long emailKey);

    @Override
    @Query("SELECT c FROM Contact c join c.phones ph WHERE ph.hashKey = :phoneKey")
    List<Contact> findPhone(@Param("phoneKey") Long phoneKey);

    @Override
    @Query(value = "SELECT * FROM contact c ORDER BY c.name LIMIT 1", nativeQuery = true)
    Contact findFirst();

} // ContactStorage
