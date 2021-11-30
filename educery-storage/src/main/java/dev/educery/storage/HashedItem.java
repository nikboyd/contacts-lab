package dev.educery.storage;

/**
 * Defines protocol for a hashed persistent item.
 *
 * <h4>HashedItem Responsibilities:</h4>
 * <ul>
 * <li>knows a surrogate key</li>
 * <li>knows a hash key</li>
 * </ul>
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface HashedItem extends SurrogatedItem {

    /**
     * @return a hash of the contents
     */
    public long hashKey();
    @Override default String formatHash() { return hashKey()+""; }

} // HashedItem
