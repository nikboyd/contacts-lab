package dev.educery.domain;

import dev.educery.codecs.ModelCodec;
import dev.educery.domain.Contact.Kind;
import dev.educery.storage.Descriptive;
import java.io.Serializable;
import javax.xml.bind.annotation.*;

/**
 * A generic contact mechanism containing a specific one and a type that indicates which kind.
 * @param <MechanismType> a mechanism type
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@XmlRootElement
@XmlSeeAlso({MailAddress.class, EmailAddress.class, PhoneNumber.class})
public class ContactMechanism<MechanismType extends Descriptive> implements Descriptive, Serializable {

    static final long serialVersionUID = 1001001L;
    public String toJSON() { return ModelCodec.from(this).toJSON(); }

    public static <MechanismType extends Descriptive>
    ContactMechanism<MechanismType> with(Contact.Kind type, MechanismType mechanism) {
        ContactMechanism result = new ContactMechanism();
        result.mechanism = mechanism;
        result.type = type;
        return result;
    }

    /**
     * a mechanism type
     */
    public String getType() { return type.name(); }
    private Contact.Kind type;

    /**
     * a specific contact mechanism
     */
    @XmlElements(value = {
        @XmlElement(name = "address", type = MailAddress.class),
        @XmlElement(name = "email", type = EmailAddress.class),
        @XmlElement(name = "phone", type = PhoneNumber.class)
    })
    public MechanismType getMechanism() { return mechanism; }
    private MechanismType mechanism;

    @Override public String description() { return formatKind(type, mechanism); }
    public static String formatKind(Kind kind, Descriptive item) { return item.formatFully(kind.name()); }

} // ContactMechanism<Mechanism>
