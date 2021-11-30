package dev.educery.storage;

import java.util.*;
import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.data.repository.CrudRepository;
import static dev.educery.utils.Exceptional.*;
import static dev.educery.utils.Utils.*;

/**
 * A persistent item with a surrogate key.
 * @param <ItemType> a kind of derived persistent item
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@MappedSuperclass
@SuppressWarnings("unchecked")
public abstract class Surrogated<ItemType> implements SurrogatedItem, Serializable {

    protected static final String Blank = " ";
    protected static final String Comma = ",";
    private static final String AND = "&&";

    protected static final String PosixSymbols = "\\p{S}";
    protected static final String PosixPunctuators = "\\p{P}";
    protected static final String AllowedSymbols = "/#";
    protected static final String ExcludedSymbols = "[^" + AllowedSymbols + "]";
    protected static final String PunctuationFilter = "[" + PosixSymbols + PosixPunctuators + AND + ExcludedSymbols + "]";
    protected static final String MultipleSpaceFilter = " +";

    /**
     * @param <ItemType> a kind of model
     * @param itemType a kind of model
     * @return a storage mechanism for a given model type
     */
    protected static <ItemType> CrudRepository<ItemType, Long> getStore(Class<?> itemType) {
        return (CrudRepository<ItemType, Long>) StorageMechanism.get(itemType); }

    /**
     * @param <StoreType> a storage type
     * @return a storage mechanism
     */
    protected <StoreType extends CrudRepository<ItemType, Long>>
        StoreType getStore() { return (StoreType) getStore(getClass()); }

    /**
     * A surrogate key, generated automatically by the configured persistence framework.
     */
    @Id @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected long key;

    @XmlAttribute
    @Override public long getKey() { return this.key; }
    public void setKey(long key) { this.key = key; }
    protected void clearKey() { this.key = 0; }

    @Override public boolean wasSaved() { return getKey() > 0; }
    @Override public ItemType asItem() { return (ItemType) this; }
    @Override public ItemType saveItem() { if (isComposite()) saveParts(); return getStore().save(this.asItem()); }

    protected boolean isComposite() { return this instanceof SurrogatedComposite; }
    protected SurrogatedComposite asComposite() { return (SurrogatedComposite) this; }
    private void saveParts() {
        saveMaps(asComposite().componentMaps());
        saveSets(asComposite().componentSets());
        saveParts(asComposite().components());
    }

    private void saveMaps(Object[] maps) { wrap(maps).forEach((m) -> saveMap((Map<Object, SurrogatedItem>) m)); }
    private <KeyType> void saveMap(Map<KeyType, SurrogatedItem> m) {
        runLoudly(() -> { // replace each mapped item with its saved version
            m.keySet().forEach((aKey) -> { m.put(aKey, (SurrogatedItem) m.get(aKey).saveItem()); });
        });
    }

    private void saveSets(Object[] sets) { wrap(sets).forEach((s) -> saveSet((Set<SurrogatedItem>) s)); }
    private void saveSet(Set<SurrogatedItem> set) {
        HashSet<SurrogatedItem> results = new HashSet<>(set);
        set.forEach((part) -> results.add((SurrogatedItem) part.saveItem()));

        // replace all items with their saved versions
        set.clear(); set.addAll(results);
    }

    private void saveParts(final SurrogatedItem[] parts) {
        if (hasNo(parts)) return; // no direct components
        final int[] x = { 0 }; // make local available to lambda
        wrap(parts).forEach((p) -> { parts[x[0]] = p.saveItem(); x[0]++; });

        // replace all parts with their saved versions if needed
        this.asComposite().components(parts);
    }

    /**
     * Removes this item from its backing store.
     * @return whether this item was removed
     */
    public boolean removeItem() {
        if (getKey() == 0) return false;
        getStore().delete(this.asItem());
        return true;
    }

    /**
     * @return this item from its backing store
     */
    public ItemType findItem() {
        if (getKey() == 0) return this.asItem();
        Optional<ItemType> result = getStore().findById(this.getKey());
        return result.isPresent() ? result.get() : null;
    }

    /**
     * Any component maps.
     * @return empty by default, override as needed
     */
    public Object[] componentMaps() { Object[] results = {}; return results; }

    /**
     * Any component sets.
     * @return empty by default, override as needed
     */
    public Object[] componentSets() { Object[] results = {}; return results; }

    /**
     * Any component items.
     * @return empty by default, override as needed
     */
    public SurrogatedItem[] components() { SurrogatedItem[] results = {}; return results; }

    /**
     * Sets the component items.
     * Derived classes that implement SurrogatedComposite override this method if needed.
     * @param components saved components
     */
    public void components(SurrogatedItem[] components) { } // override this if needed

    /**
     * Normalizes text with full capitalization, without punctuation, and without extraneous white space.
     * @param text some text
     * @return normalized text
     */
    public static String normalizeWords(String text) {
        return WordUtils.capitalizeFully(StringUtils.defaultString(text).trim())
            .replaceAll(PunctuationFilter, Empty).replaceAll(MultipleSpaceFilter, Blank); }

    /**
     * Normalizes code as upper case.
     * @param codeText code text
     * @return a normalized code
     */
    public static String normalizeCode(String codeText) {
        return StringUtils.defaultString(codeText).trim().toUpperCase(); }

} // Surrogated<ItemType>
