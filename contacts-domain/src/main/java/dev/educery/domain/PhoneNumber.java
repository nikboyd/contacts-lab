package dev.educery.domain;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.*;

import dev.educery.storage.Hashed;
import dev.educery.codecs.ModelCodec;

/**
 * A (unique) phone number.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@Entity
@Table(name = "phone_number", indexes = {
    @Index(name = "ix_phone_hash", columnList = "hash_key")})
@XmlRootElement(name = "PhoneNumber", namespace = "##default")
@SuppressWarnings("unchecked")
public class PhoneNumber extends Hashed<PhoneNumber> implements Serializable {

    static final long serialVersionUID = 1001001L;
    static final PhoneNumber SamplePhone = PhoneNumber.from("999-999-9999");
    public PhoneNumber() { super(); }

    /**
     * @return a count of saved phone numbers
     */
    public static int count() { return (int) SamplePhone.getStore().count(); }
    public static PhoneNumber fromJSON(String json) { return ModelCodec.to(PhoneNumber.class).fromJSON(json); }
    public String toJSON() { return ModelCodec.from(this).toJSON(); }

    /**
     * @param phoneNumber a formatted phone number
     * @return a new PhoneNumber
     * @exception IllegalArgumentException if the supplied phone number cannot be parsed
     */
    public static PhoneNumber from(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches(PATTERN)) {
            throw new IllegalArgumentException(MESSAGE);
        }

        PhoneNumber result = new PhoneNumber();
        result.setFormattedNumber(phoneNumber);
        return result;
    }

    static final String DASH = "-";
    static final String[] NoMessage = { };
    static final String FORMAT = "999-999-9999";
    static final String PATTERN = "(\\d{3})-(\\d{3})-(\\d{4})";
    static final String MESSAGE = "phone numbers must have a format like " + FORMAT;
    static final String[] FormatMessage = { MESSAGE };
    public static String[] validate(String candidate) {
        return (candidate == null || !candidate.matches(PATTERN)) ? FormatMessage : NoMessage;
    }


    @Column(name = "phone_area", nullable = false, length = 3)
    private String areaCode = "";

    @Column(name = "phone_prefix", nullable = false, length = 3)
    private String prefix = "";

    @Column(name = "phone_suffix", nullable = false, length = 4)
    private String suffix = "";

    /**
     * a formatted phone number
     */
    @XmlAttribute(name = "value")
    public String getFormattedNumber() { return formatNumber(); }
    public void setFormattedNumber(String phoneNumber) {
        String[] parts = phoneNumber.split(DASH);
        this.areaCode = parts[0];
        this.prefix = parts[1];
        this.suffix = parts[2];
    }

    @Override protected long hash() { return hash(formatNumber()); }
    @Override public int hashCode() { String hashSource = formatNumber(); return hashSource.hashCode(); }
    @Override public boolean equals(Object candidate) {
        if (candidate == null || getClass() != candidate.getClass()) return false;
        final PhoneNumber other = (PhoneNumber) candidate;
        return other.formatNumber().equals(formatNumber());
    }

    @Override public String formatValue() { return formatNumber(); }
    static final String PhoneForm = "%s-%s-%s";
    public String formatNumber() { return String.format(PhoneForm, areaCode, prefix, suffix); }

} // PhoneNumber
