package dev.educery.storage;

import dev.educery.domain.PhoneNumber;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * A storage mechanism for phone numbers.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface PhoneStorage
        extends CrudRepository<PhoneNumber, Long>, Hashed.Search<PhoneNumber> {

    @Query("SELECT p FROM PhoneNumber p WHERE p.key = :key")
    PhoneNumber findKey(@Param("key") Long key);

    @Override
    @Query("SELECT p FROM PhoneNumber p WHERE p.hashKey = :hashKey")
    PhoneNumber findHash(@Param("hashKey") Long key);

} // PhoneStorage
