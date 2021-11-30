package dev.educery.storage;

/**
 * A composition of surrogate items, some of whose components are held in maps or sets.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface SurrogatedComposite extends SurrogatedItem {

    /**
     * Returns any maps that contain surrogate components. Due to the limitations of generic maps, their actual type
     * signatures are erased. The framework used to save the components contained in the maps will determine their
     * actual types.
     *
     * @return any component maps
     */
    public Object[] componentMaps();

    /**
     * Returns any sets that contain surrogate components. Due to the limitations of generic sets, their actual type
     * signatures are erased. The framework used to save the components contained in the sets will determine their
     * actual types.
     *
     * @return any component sets
     */
    public Object[] componentSets();

    /**
     * Any directly related surrogate components.
     */
    public SurrogatedItem[] components();
    public void components(SurrogatedItem[] components);

} // SurrogatedComposite
