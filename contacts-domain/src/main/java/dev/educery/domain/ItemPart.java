package dev.educery.domain;

import java.io.Serializable;
import javax.xml.bind.annotation.*;

import dev.educery.codecs.ModelCodec;
import static dev.educery.storage.Surrogated.*;

/**
 * Describes a contact or part to be added.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@XmlRootElement
public class ItemPart implements Serializable {

    static final long serialVersionUID = 1001001L;

    public String toJSON() { return ModelCodec.from(this).toJSON(); }
    public static ItemPart fromJSON(String json) { return ModelCodec.to(ItemPart.class).fromJSON(json); }

    /**
     * a contact name
     */
    public String getName() { return this.name; }
    public void setName(String value) { this.name = normalizeWords(value); }
    public ItemPart withName(String value) { setName(value); return this; }
    protected String name = "  ";

    /**
     * a part description with [ type, kind, value ]
     */
    public String[] getDescription() { return this.description; }
    protected String[] description = { "type", "kind", "value" };
    private ItemPart onlyName() { return prepared("name"); }
    private ItemPart prepared(String ... values) { description = values; return this; }

    public Contact.Type type() { return Contact.Type.valueOf(description[0]); }
    public ItemPart withType(Contact.Type value) { return withType(value.name()); }
    public ItemPart withType(String value) {
        return (value.equals("name")) ? onlyName() : prepared(value, "", ""); }

    public Contact.Kind kind() { return Contact.Kind.valueOf(description[1]); }
    public ItemPart withKind(String value) { description[1] = value; return this; }
    public ItemPart withKind(Contact.Kind value) { return withKind(value.name()); }

    public String value() { return description[2]; }
    public ItemPart withValue(String value) { description[2] = value; return this; }

    public ItemPart() { }
    public ItemPart(String name) { this.name = name; }
    public static ItemPart contact(String name) { return new ItemPart(name).withType(Contact.Type.name); }
    public ItemPart with(Contact.Kind kind, PhoneNumber phone) {
        return this.withType(Contact.Type.phone).withKind(kind).withValue(phone.formatNumber()); }
    public ItemPart with(Contact.Kind kind, EmailAddress email) {
        return this.withType(Contact.Type.email).withKind(kind).withValue(email.formatAddress()); }
    public ItemPart with(Contact.Kind kind, MailAddress mail) {
        return this.withType(Contact.Type.mail).withKind(kind).withValue(mail.formatAddress()); }

} // ItemPart
