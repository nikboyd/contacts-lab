package dev.educery.storage;

import dev.educery.utils.Logging;

/**
 * Defines protocol for description of an item.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Descriptive extends Logging {

    default String formatKey() { return Empty; }
    default String formatHash() { return Empty; }
    default String formatValue() { return Empty; }

    /**
     * Logs a description of this item.
     */
    default void describe() { report(description()); }
    default void describeHash() { report(hashDescription()); }

    public static final String ItemForm = "%s='%s'";
    default String description() { return String.format(ItemForm, getClass().getSimpleName(), formatValue()); }

    public static final String HashForm = "%s, hash=%s, key=%s";
    default String hashDescription() { return String.format(HashForm, description(), formatHash(), formatKey()); }

    static final String FullForm = "%s:%s";
    default String formatFully(String prefix) { return String.format(FullForm, prefix, hashDescription()); }


} // Descriptive
