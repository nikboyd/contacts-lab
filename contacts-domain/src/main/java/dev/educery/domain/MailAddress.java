package dev.educery.domain;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.validation.constraints.*;

import dev.educery.storage.Hashed;
import dev.educery.codecs.ModelCodec;
import static dev.educery.utils.Utils.hasNo;
import dev.educery.validations.ModelValidator;
import org.springframework.util.StringUtils;

/**
 * A (unique) mailing address.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@Entity
@Table(name = "mail_address", indexes = {
    @Index(name = "ix_address_hash", columnList = "hash_key")})
@XmlRootElement(name = "MailAddress", namespace = "##default")
@SuppressWarnings("unchecked")
public class MailAddress extends Hashed<MailAddress> implements Serializable {

    static final long serialVersionUID = 1001001L;
    static final MailAddress SampleAddress = new MailAddress();
    public MailAddress() {
        super();
        this.street = Empty;
        this.office = Empty;
        this.city = Empty;
        this.stateCode = Empty;
        this.postalCode = Empty;
    }

    //private static final String CountryCodeValidationPattern = "[A-Z]{3}"; // must be a code with 3 upper case letters
    static final String[] NoMessage = { };
    static final String FORMAT = "1234 Main St, Los Angeles, CA 90066";
    static final String MESSAGE = "mail addresses must have a format like " + FORMAT;
    static final String[] FormatMessage = { MESSAGE };
    public static String[] validate(String candidate) {
        if (candidate == null) return FormatMessage;
        MailAddress test = MailAddress.from(candidate);
        if (test == null) return FormatMessage;
        return test.validate();
    }

    /**
     * @return a count of saved addresses
     */
    public static int count() { return (int) SampleAddress.getStore().count(); }
    public static MailAddress fromJSON(String json) { return ModelCodec.to(MailAddress.class).fromJSON(json); }
    public String toJSON() { return ModelCodec.from(this).toJSON(); }
    protected MailAddress makeDirty() { markDirty(); return this; }

    /**
     * a street number, name, and type
     */
    @XmlAttribute(name = "street")
    public String getStreet() { return this.street; }
    public void setStreet(String street) { this.street = normalizeWords(street); }
    public MailAddress withStreet(String street) { setStreet(street); return makeDirty(); }

    static final String StreetAddressValidationPattern = "((\\d+\\s)[\\w\\s/#]+){0,1}"; // must be number(s) + name(s)
    @Column(name = "street", nullable = true, length = 50)
    @Size(min = 0, max = 50, message = MESSAGE)
    @Pattern(regexp = StreetAddressValidationPattern, message = MESSAGE)
    protected String street;

    /**
     * a building unit (office)
     */
    @XmlAttribute(name = "office")
    public String getOffice() { return this.office; }
    public void setOffice(String office) { this.office = normalizeWords(office); }
    public MailAddress withOffice(String office) { setOffice(office); return makeDirty(); }

    static final String BuildingUnitValidationPattern = "[\\w\\s/#]*"; // must be some word(s) and number(s)
    @Column(name = "office", nullable = true, length = 50)
    @Size(min = 0, max = 50, message = MESSAGE)
    @Pattern(regexp = BuildingUnitValidationPattern, message = MESSAGE)
    protected String office;

    /**
     * a city name
     */
    @XmlAttribute(name = "city")
    public String getCity() { return this.city; }
    public void setCity(String city) { this.city = normalizeWords(city); }
    public MailAddress withCity(String city) { setCity(city); return makeDirty(); }

    static final String CityNameValidationPattern = "[a-zA-Z\\s]+"; // must be some word(s)
    @Column(name = "city", nullable = false, length = 50)
    @Size(min = 5, max = 50, message = MESSAGE)
    @Pattern(regexp = CityNameValidationPattern, message = MESSAGE)
    protected String city;

    /**
     * a state code (abbreviation)
     */
    @XmlAttribute(name = "state")
    public String getStateCode() { return this.stateCode; }
    public void setStateCode(String stateCode) { this.stateCode = normalizeCode(stateCode); }
    public MailAddress withStateCode(String stateCode) { setStateCode(stateCode); return makeDirty(); }

    static final String StateCodeValidationPattern = "[A-Z]{2}"; // must be a code with 2 upper case letters
    @Column(name = "state_code", nullable = false, length = 2)
    @Pattern(regexp = StateCodeValidationPattern, message = MESSAGE)
    protected String stateCode;

    /**
     * a postal code
     */
    @XmlAttribute(name = "zip")
    public String getPostalCode() { return this.postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = normalizeCode(postalCode); }
    public MailAddress withPostalCode(String postalCode) { setPostalCode(postalCode); return makeDirty(); }

    static final String PostalCodeValidationPattern = "[\\w\\s]+"; // must be a code with some number(s) and/or word(s)
    @Column(name = "postal_code", nullable = false, length = 15)
    @Size(min = 5, max = 15, message = MESSAGE)
    @Pattern(regexp = PostalCodeValidationPattern, message = MESSAGE)
    protected String postalCode;

    /**
     * Builds a new MailAddress.
     * @param parts parts of the address
     * @return a new MailAddress if provided valid parts
     */
    public static MailAddress from(String... parts) {
        if (hasNo(parts)) return null; // not a full address
        if (parts.length == 1) {
            if (parts[0].contains(Comma)) return from(parts[0].split(Comma));
            return null; // not a full address
        }
        if (parts.length == 3) {
            String[] state = parts[2].trim().split(Blank);
            return with(parts[0].trim(), parts[1].trim(), state[0].trim(), state[1].trim());
        }
        if (parts.length == 4) {
            String test = parts[3].trim();
            if (test.contains(Blank)) {
                String[] state = test.split(Blank);
                return with(parts[0].trim(), parts[1].trim(), parts[2].trim(), state[0].trim(), state[1].trim());
            } else {
                return with(parts[0].trim(), parts[1].trim(), parts[2].trim(), test);
            }
        }
        if (parts.length == 5) {
            return with(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim());
        }
        return null; // not a full address
    }

    /**
     * Builds a new MailAddress.
     * @param street a street number and name
     * @param city a city name
     * @param stateCode a state code
     * @param postalCode a postal code
     * @return a new MailAddress
     */
    public static MailAddress with(String street, String city, String stateCode, String postalCode) {
        return with(street, Empty, city, stateCode, postalCode);
    }

    /**
     * Builds a new MailAddress.
     * @param street a street number and name
     * @param unit a building unit
     * @param city a city name
     * @param stateCode a state code
     * @param postalCode a postal code
     * @return a new MailAddress
     */
    public static MailAddress with(String street, String unit, String city, String stateCode, String postalCode) {
        return new MailAddress()
            .withStreet(street)
            .withOffice(unit)
            .withCity(city)
            .withStateCode(stateCode)
            .withPostalCode(postalCode);
    }

    @Override protected long hash() { return hash(formatAddress()); }
    @Override public int hashCode() { String hashSource = formatAddress(); return Math.abs(hashSource.hashCode()); }
    @Override public boolean equals(Object candidate) {
        if (candidate == null || getClass() != candidate.getClass()) return false;
        final MailAddress other = (MailAddress) candidate;
        return other.formatAddress().equals(formatAddress());
    }

    /**
     * @return any problems detected after validation
     */
    public String[] validate() { return ModelValidator.getConfiguredValidator().validate(this); }

    @Override public String formatValue() { return formatAddress(); }
    static final String ShortAddress = "%s, %s, %s %s";
    static final String FullAddress = "%s, %s, %s, %s %s";
    public String formatAddress() {
        return getOffice().isEmpty() ?
            String.format(ShortAddress, getStreet(), getCity(), getStateCode(), getPostalCode()) :
            String.format(FullAddress, getStreet(), getOffice(), getCity(), getStateCode(), getPostalCode()); }

} // MailAddress
