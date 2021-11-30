package dev.educery.domain;

import java.io.Serializable;
import javax.mail.internet.AddressException;
import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.mail.internet.InternetAddress;

import dev.educery.storage.Hashed;
import dev.educery.codecs.ModelCodec;

/**
 * An (unique) email address.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@Entity
@Table(name = "email_address", indexes = {
    @Index(name = "ix_email_hash", columnList = "hash_key")})
@XmlRootElement(name = "EmailAddress", namespace = "##default")
@SuppressWarnings("unchecked")
public class EmailAddress extends Hashed<EmailAddress> implements Serializable {

    static final long serialVersionUID = 1001001L;
    static final EmailAddress SampleAddress = EmailAddress.from("sample@business.com");
    public EmailAddress() { super(); }

    /**
     * @return a count of saved email addresses
     */
    public static int count() { return (int) SampleAddress.getStore().count(); }
    public static EmailAddress fromJSON(String json) { return ModelCodec.to(EmailAddress.class).fromJSON(json); }
    public String toJSON() { return ModelCodec.from(this).toJSON(); }

    /**
     * Returns a new EmailAddress.
     *
     * @param emailAddress a formatted email address
     * @return a new EmailAddress
     * @exception IllegalArgumentException if the supplied email address cannot be parsed
     */
    @SuppressWarnings("unused")
    public static EmailAddress from(String emailAddress) {
        try {
            InternetAddress test = new InternetAddress(emailAddress, true);
        } catch (AddressException e) {
            throw new IllegalArgumentException(MESSAGE, e);
        }

        EmailAddress result = new EmailAddress();
        String[] parts = emailAddress.split(AT);
        result.account = parts[0];
        result.hostName = parts[1];
        return result;
    }

    static final String AT = "@";
    static final String[] NoMessage = { };
    static final String FORMAT = "account@host.com";
    static final String MESSAGE = "email addresses must have a format like " + FORMAT;
    static final String[] FormatMessage = { MESSAGE };
    public static String[] validate(String candidate) {
        try {
            InternetAddress test = new InternetAddress(candidate, true);
            return NoMessage;
        } catch (AddressException e) {
            return FormatMessage;
        }
    }


    @Column(name = "account", nullable = false, length = 30)
    private String account = Empty;

    @Column(name = "hostname", nullable = false, length = 30)
    private String hostName = Empty;

    /**
     * a formatted email address
     */
    @XmlAttribute(name = "value")
    public String getFormattedAddress() { return formatAddress(); }
    public void setFormattedAddress(String emailAddress) {
        String[] parts = emailAddress.split(AT);
        this.account = parts[0];
        this.hostName = parts[1];
    }

    @Override protected long hash() { return hash(formatAddress()); }
    @Override public int hashCode() { String hashSource = formatAddress(); return hashSource.hashCode(); }
    @Override public boolean equals(Object candidate) {
        if (candidate == null || getClass() != candidate.getClass()) return false;
        final EmailAddress other = (EmailAddress) candidate;
        return other.formatAddress().equals(formatAddress());
    }

    @Override public String formatValue() { return formatAddress(); }
    public String formatAddress() { return this.account + AT + this.hostName; }

} // EmailAddress
