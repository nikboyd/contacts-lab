package dev.educery.storage;

import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import org.springframework.data.repository.CrudRepository;
import dev.educery.crypto.LongHash;
import static dev.educery.utils.Utils.hasNone;

/**
 * An immutable item uniquely identified using a hash of its contents.
 * @param <ItemType> a kind of derived persistent item
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@MappedSuperclass
@SuppressWarnings("unchecked")
public abstract class Hashed<ItemType>
        extends Surrogated<ItemType> implements HashedItem {

    /**
     * Defines protocol for searching for a hashed item.
     * @param <ItemType> an item type
     */
    public static interface Search<ItemType> extends CrudRepository<ItemType, Long> {

        /**
         * Finds a hashed item.
         *
         * @param hashKey a hash key value
         * @return a hashed item, or null
         */
        public ItemType findHash(Long hashKey);

    } // Search

    protected Search<ItemType> getSearchStore() { return (Search<ItemType>) getStore(); }

    @Override public ItemType asItem() { return (ItemType) this; }

    /**
     * A hash of the item contents.
     */
    @Override public long hashKey() { prepareHash(); return this.hashKey; }
    @Column(name = "hash_key", nullable = false) protected long hashKey = 0;

    protected abstract long hash();
    protected void resetHash() { this.hashKey = 0; }
    protected void markDirty() { resetHash(); clearKey(); }
    public void prepareHash() { if (hashKey == 0) this.hashKey = hash(); }
    public long hash(String value) { return LongHash.hash(value); }

    /**
     * Finds this item with its hash.
     */
    public ItemType findWithHash() { prepareHash(); return getSearchStore().findHash(hashKey()); }

    /**
     * Finds this item with its hash, or key if previously saved.
     */
    @Override public ItemType findItem() {
        if (!wasSaved()) return findWithHash();
        Optional<ItemType> result = getStore().findById(this.getKey());
        return result.isPresent() ? result.get() : null;
    }

    /**
     * Saves this (immutable) item.
     */
    @Override public ItemType saveItem() {
        if (wasSaved()) return isComposite() ? super.saveItem() : findItem();
        ItemType result = findWithHash();
        return hasNone(result) ? super.saveItem() : result;
    }

} // Hashed<ItemType>
