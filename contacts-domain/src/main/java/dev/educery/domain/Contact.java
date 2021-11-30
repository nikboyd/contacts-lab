package dev.educery.domain;

import java.util.*;
import java.io.Serializable;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.validation.constraints.Size;

import dev.educery.storage.Hashed;
import dev.educery.codecs.ModelCodec;
import dev.educery.storage.Descriptive;
import dev.educery.storage.StorageMechanism;
import dev.educery.storage.SurrogatedComposite;
import static dev.educery.storage.Surrogated.normalizeWords;
import static dev.educery.utils.Logging.isEmpty;
import static dev.educery.utils.Utils.*;

/**
 * A (unique) named composite of contact information.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@Entity
@Table(name = "contact", indexes = {
    @Index(name = "ix_name_hash", columnList = "hash_key")})
@XmlRootElement(name = "Contact", namespace = "##default")
@SuppressWarnings("unchecked")
public class Contact extends Hashed<Contact> implements SurrogatedComposite, Serializable {

    /**
     * Defines ways to find contacts.
     */
    public static interface IContactSearch extends Search<Contact> {

        Contact findFirst();
        Contact findKey(Long key);
        List<Contact> findLike(String sample);
        List<Contact> findEmail(Long emailKey);
        List<Contact> findPhone(Long phoneKey);

    } // IContactSearch

    static final long serialVersionUID = 1001001L;
    static final Contact SampleContact = new Contact();
    public Contact() { super(); }

    /**
     * Indicates a kind of contact.
     */
    public static enum Kind { HOME, WORK, MOBILE, BILLING, SHIPPING, } // Kind

    /**
     * Identifies a kind of contact ID.
     */
    public static enum Type { name, email, phone, mail, } // Type

    /**
     * @return a count of saved contacts
     */
    public static int count() { return (int) SampleContact.getStore().count(); }
    public static Contact named(String name) { return new Contact().withName(name); }
    public static Contact findKey(long key) { return storage().findKey(key); }

    public ItemBrief brief() { return new ItemBrief(getKey(), "name="+getName()); }
    public String toJSON() { return ModelCodec.from(this).toJSON(); }
    public static Contact fromJSON(String json) { return ModelCodec.to(Contact.class).fromJSON(json); }
    public static List<Contact> listFromJSON(String listJSON) {
        return wrap(ModelCodec.to(Contact[].class).fromJSON(listJSON)); }

    public static IContactSearch storage() { return StorageMechanism.get(Contact.class); }
    public static List<Contact> like(String text) { return named(text).findSimilar(); }

    public List<Contact> findSimilar() { return storage().findLike(getLikeness()); }
    public static Contact findFirst() { return storage().findFirst(); }
    public static List<Contact> findSimilar(EmailAddress email) { return storage().findEmail(email.hashKey()); }
    public static List<Contact> findSimilar(PhoneNumber phone) { return storage().findPhone(phone.hashKey()); }
    public static Contact find(String name) { return Contact.named(name).findWithHash(); }
    public static List<Contact> findNamed(String name) {
        Contact c = Contact.find(name); if (hasNone(c)) return new ArrayList();
        Contact[] cs = { c }; return wrap(cs); }

    /**
     * a contact name
     */
    @XmlAttribute(name = "name")
    public String getName() { return this.name; }
    public void setName(String value) { this.name = normalizeWords(value); }
    public Contact withName(String value) { setName(value); markDirty(); return this; }

    @Column(name = "name", nullable = false, length = 100)
    @Size(min = 2, max = 100, message = "contact name too short or long")
    protected String name = "  ";

    static final String Wild = "%";
    protected String getLikeness() { return getName().isEmpty() ? Wild : Wild + getName() + Wild; }

    @Override protected long hash() { return hash(getName()); }
    @Override public int hashCode() { String hashSource = getName(); return hashSource.hashCode(); }
    @Override public boolean equals(Object candidate) {
        if (hasNone(candidate) || getClass() != candidate.getClass()) return false;
        return hashCode() == candidate.hashCode();
    }

    /**
     * associated contact mechanisms
     */
    @XmlElement(name = "mechanisms")
    public List<ContactMechanism> getMechanisms() {
        ArrayList<ContactMechanism> results = new ArrayList();
        addresses.keySet().forEach((addressType) -> {
            results.add(ContactMechanism.with(addressType, addresses.get(addressType)));
        });

        phones.keySet().forEach((phoneType) -> {
            results.add(ContactMechanism.with(phoneType, phones.get(phoneType)));
        });

        emails.keySet().forEach((emailType) -> {
            results.add(ContactMechanism.with(emailType, emails.get(emailType)));
        });
        return results;
    }

    public void setMechanisms(List<ContactMechanism> mechanisms) {
        for (ContactMechanism m : mechanisms) {
            if (m.getMechanism() instanceof MailAddress)  adoptMail(m);
            if (m.getMechanism() instanceof EmailAddress) adoptEmail(m);
            if (m.getMechanism() instanceof PhoneNumber)  adoptPhone(m);
        }
    }


    /**
     * @return the component maps associated with this Contact
     */
    @Override public Object[] componentMaps() {
        Object[] results = { this.addresses, this.emails, this.phones }; return results;
    }

    // component map indices for this composite
    static final int AddressIndex = 0;
    static final int EmailIndex = 1;
    static final int PhoneIndex = 2;

    /**
     * Any mail addresses associated with this contact.
     */
    @OneToMany(
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "kind", length = 10, nullable = false)
    private final Map<Kind, MailAddress> addresses = new HashMap<>();
    private void adoptMail(ContactMechanism m) { adoptAddress(Kind.valueOf(m.getType()), (MailAddress) m.getMechanism()); }
    private void adoptAddress(Kind kind, MailAddress address) {
        if (hasOne(address)) addresses.put(kind, address); else addresses.remove(kind);
    }

    public int countAddresses() { return this.addresses.size(); }
    public MailAddress getAddress(Kind kind) { return this.addresses.get(kind); }
    public boolean hasAddress(Kind kind) { return this.addresses.containsKey(kind); }
    public Contact removeAddress(final Kind kind) { adoptAddress(kind, null); return this; }
    public Contact with(Kind kind, MailAddress address) { return withAddress(kind, address); }
    public Contact withAddress(Kind kind, MailAddress address) { adoptAddress(kind, address); return this; }

    /**
     * Any phone numbers associated with this contact.
     */
    @OneToMany(
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "kind", length = 10, nullable = false)
    private final Map<Kind, PhoneNumber> phones = new HashMap<>();
    private void adoptPhone(ContactMechanism m) { adoptPhone(Kind.valueOf(m.getType()), (PhoneNumber) m.getMechanism()); }
    private void adoptPhone(Kind kind, PhoneNumber phone) {
        if (hasOne(phone)) phones.put(kind, phone); else phones.remove(kind);
    }

    public int countPhones() { return this.phones.size(); }
    public PhoneNumber getPhone(Kind kind) { return this.phones.get(kind); }
    public boolean hasPhone(Kind kind) { return this.phones.containsKey(kind); }
    public Contact removePhone(Kind kind) { adoptPhone(kind, null); return this; }
    public Contact with(Kind kind, PhoneNumber phone) { return withPhone(kind, phone); }
    public Contact withPhone(Kind kind, PhoneNumber phone) { adoptPhone(kind, phone); return this; }

    /**
     * Any email addresses associated with this contact.
     */
    @OneToMany(
        fetch = FetchType.EAGER,
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "kind", length = 10, nullable = false)
    private final Map<Kind, EmailAddress> emails = new HashMap<>();
    private void adoptEmail(ContactMechanism m) { adoptEmail(Kind.valueOf(m.getType()), (EmailAddress) m.getMechanism()); }
    private void adoptEmail(Kind kind, EmailAddress email) {
        if (hasOne(email)) emails.put(kind, email); else emails.remove(kind);
    }

    public int countEmails() { return this.emails.size(); }
    public EmailAddress getEmail(Kind kind) { return this.emails.get(kind); }
    public boolean hasEmail(Kind kind) { return this.emails.containsKey(kind); }
    public Contact removeEmail(final Kind kind) { adoptEmail(kind, null); return this; }
    public Contact with(Kind kind, EmailAddress email) { return withEmail(kind, email); }
    public Contact withEmail(Kind kind, EmailAddress email) { adoptEmail(kind, email); return this; }

    @Override public String formatValue() { return getName(); }
    @Override public void describe() {
        report(hashDescription());
        phones.keySet().forEach((kind) -> { report(phones.get(kind).formatFully(kind.name())); });
        emails.keySet().forEach((kind) -> { report(emails.get(kind).formatFully(kind.name())); });
        addresses.keySet().forEach((kind) -> { report(addresses.get(kind).formatFully(kind.name())); });
    }

    static final String MessageFormat = "%s %s";
    private String formatMessage(Kind k, List<String> messages) { return format(MessageFormat, k.name(), messages.get(0)); }

    public void mergePhone(Kind k, String text, List<String> messages) {
        if (isEmpty(text)) {
            // remove any existing value
            if (hasPhone(k)) removePhone(k);
        }
        else { // check existing value
            List<String> notes = wrap(PhoneNumber.validate(text));
            if (!notes.isEmpty()) {
                messages.add(formatMessage(k, notes));
                return; // bail out now
            }

            PhoneNumber value = PhoneNumber.from(text);
            if (hasPhone(k)) {
                PhoneNumber current = getPhone(k);
                if (!current.formatValue().equals(text)) {
                    // replace existing value
                    withPhone(k, value);
                }
            }
            else { // add new value
                withPhone(k, value);
            }
        }
    }

    public void mergeEmail(Kind k, String text, List<String> messages) {
        if (isEmpty(text)) {
            // remove any existing value
            if (hasEmail(k)) removeEmail(k);
        }
        else { // check existing value
            List<String> notes = wrap(EmailAddress.validate(text));
            if (!notes.isEmpty()) {
                messages.add(formatMessage(k, notes));
                return; // bail out now
            }

            EmailAddress value = EmailAddress.from(text);
            if (hasEmail(k)) {
                EmailAddress current = getEmail(k);
                if (!current.formatValue().equals(text)) {
                    // replace existing value
                    withEmail(k, value);
                }
            }
            else { // add new value
                withEmail(k, value);
            }
        }
    }

    public void mergeAddress(Kind k, String text, List<String> messages) {
        if (isEmpty(text)) {
            // remove any existing value
            if (hasAddress(k)) removeAddress(k);
        }
        else { // check existing value
            List<String> notes = wrap(MailAddress.validate(text));
            if (!notes.isEmpty()) {
                messages.add(formatMessage(k, notes));
                return; // bail out now
            }

            MailAddress value = MailAddress.from(text);
            if (hasAddress(k)) {
                MailAddress current = getAddress(k);
                if (!current.formatValue().equals(text)) {
                    // replace existing value
                    withAddress(k, value);
                }
            }
            else { // add new value
                withAddress(k, value);
            }
        }
    }

    static final String DupeFormat = "'%s' duplicates existing %s";
    private static String formatDupe(Descriptive d, String type) {
        return String.format(DupeFormat, d.formatValue(), type); }

    public static List<String> checkParts(Contact c) {
        List<String> messages = new ArrayList();
        checkName(c, messages);
        checkPhone(c, Kind.HOME, messages);
        checkPhone(c, Kind.WORK, messages);
        checkPhone(c, Kind.MOBILE, messages);
        checkEmail(c, Kind.HOME, messages);
        checkEmail(c, Kind.WORK, messages);
        checkAddress(c, Kind.HOME, messages);
        checkAddress(c, Kind.WORK, messages);
        checkAddress(c, Kind.BILLING, messages);
        checkAddress(c, Kind.SHIPPING, messages);
        return messages;
    }

    private static void checkName(Contact c, List<String> messages) {
        if (c.getKey() == 0) {
            Contact item = c.findWithHash();
            if (hasOne(item)) {
                messages.add(formatDupe(item, "contact"));
            }
        }
    }

    private static void checkPhone(Contact c, Kind k, List<String> messages) {
        if (c.hasPhone(k)) {
            PhoneNumber item = c.getPhone(k);
            if (item.getKey() == 0) {
                PhoneNumber found = item.findWithHash();
                if (hasOne(found)) {
                    messages.add(formatDupe(item, "phone number"));
                }
            }
        }
    }

    private static void checkEmail(Contact c, Kind k, List<String> messages) {
        if (c.hasEmail(k)) {
            EmailAddress item = c.getEmail(k);
            if (item.getKey() == 0) {
                EmailAddress found = item.findWithHash();
                if (hasOne(found)) {
                    messages.add(formatDupe(item, "email address"));
                }
            }
        }
    }

    private static void checkAddress(Contact c, Kind k, List<String> messages) {
        if (c.hasAddress(k)) {
            MailAddress item = c.getAddress(k);
            if (item.getKey() == 0) {
                MailAddress found = item.findWithHash();
                if (hasOne(found)) {
                    messages.add(formatDupe(item, "mail address"));
                }
            }
        }
    }

} // Contact
