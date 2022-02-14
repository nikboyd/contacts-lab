package dev.educery.facets;

import java.util.*;
import java.nio.file.Paths;
import one.microstream.storage.embedded.types.*;

import dev.educery.domain.Contact;
import static dev.educery.utils.Utils.*;

/**
 * Contains registered contacts.
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ContactRegistry {

    static final String[] Location = { "target", "contact-data", };
    static final EmbeddedStorageManager StoreManager =
        EmbeddedStorage.start(new ContactRegistry(), Paths.get(".", Location));

    private final HashMap<String, Contact> contacts = new HashMap();
    public void register(Contact c) { this.contacts.put(c.getName(), c); }
    public Contact find(String name) { return this.contacts.get(name); }
    public Map<String, Contact> findLike(String pattern) {
        return selectFrom(this.contacts, (k, c) -> c.getName().matches(pattern)); }

} // ContactRegistry
