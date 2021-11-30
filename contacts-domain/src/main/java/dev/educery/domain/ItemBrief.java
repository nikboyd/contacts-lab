package dev.educery.domain;

import java.util.*;
import java.io.Serializable;
import javax.xml.bind.annotation.*;

import dev.educery.codecs.ModelCodec;
import dev.educery.storage.SurrogatedItem;
import static dev.educery.utils.Utils.wrap;

/**
 * A brief item description.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@XmlRootElement
public class ItemBrief implements Serializable {

    static final long serialVersionUID = 1001001L;

    /**
     * an item key
     */
    public long getKey() { return key; }
    public void setKey(long key) { this.key = key; }
    private long key;

    /**
     * an item type or other description
     */
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    private String type;

    public ItemBrief() { }
    public ItemBrief(long key, String type) { this.key = key; this.type = type; }
    public static ItemBrief from(SurrogatedItem item) {
        return new ItemBrief(item.getKey(), item.description()); }

    public static ItemBrief fromJSON(String json) { return ModelCodec.to(ItemBrief.class).fromJSON(json); }
    public String toJSON() { return ModelCodec.from(this).toJSON(); }

    public static List<ItemBrief> listFromJSON(String listJSON) {
        return wrap(ModelCodec.to(ItemBrief[].class).fromJSON(listJSON)); }

    static final String TextForm = "%s = %d";
    @Override public String toString() { return String.format(TextForm, getType(), getKey()); }

} // ItemBrief
