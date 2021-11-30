package dev.educery.storage;

import dev.educery.utils.Logging;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Defines protocol for a persistent item.
 *
 * <h4>SurrogatedItem Responsibilities:</h4>
 * <ul>
 * <li>knows a surrogate key</li>
 * </ul>
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface SurrogatedItem extends Descriptive {

    /**
     * @return a surrogate key value
     */
    public long getKey();
    @Override default String formatKey() { return getKey()+""; }

    /**
     * @return whether this item was saved
     */
    @XmlTransient public boolean wasSaved();

    /**
     * @param <ItemType> an item type
     * @return this item
     */
    public <ItemType> ItemType asItem();

    /**
     * Saves this item.
     * @param <ItemType> an item type
     * @return this item
     */
    public <ItemType> ItemType saveItem();

} // SurrogatedItem
