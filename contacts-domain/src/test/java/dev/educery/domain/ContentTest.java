package dev.educery.domain;

import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

import dev.educery.codecs.ModelCodec;
import dev.educery.domain.Contact.Kind;
import static dev.educery.utils.Utils.*;
import dev.educery.utils.Logging;

/**
 * Confirms proper formatting of content.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ContentTest implements Logging {

    static final String TestName = "George Jungleman";
    static final String TestPhone = "888-888-8888";
    static final String TestEmail = "george@jungleman.com";
    static final String TestMail  = "1234 Main St, Anytown, CA 94005";
    static final String FullMail  = "1234 Main St, Suite 2000, Anytown, CA 94005";

    @Test public void partCodec() {
        ItemPart p = new ItemPart();
        report(p.toJSON());

        p = ItemPart.contact(TestName);
        report(p.toJSON());

        p = ItemPart.contact(TestName).with(Kind.HOME, PhoneNumber.from(TestPhone));
        report(p.toJSON());

        p = ItemPart.contact(TestName).with(Kind.HOME, EmailAddress.from(TestEmail));
        report(p.toJSON());

        p = ItemPart.contact(TestName).with(Kind.HOME, MailAddress.from(TestMail));
        report(p.toJSON());

        p = ItemPart.contact(TestName).with(Kind.HOME, MailAddress.from(FullMail));
        report(p.toJSON());
    }

    @Test public void briefCodec() {
        PhoneNumber sample = PhoneNumber.from(TestPhone);
        sample.setKey(88888);

        ItemBrief b = ItemBrief.from(sample);
        report(b.toJSON());

        ItemBrief d = ItemBrief.fromJSON(b.toJSON());
        assertTrue(d.getKey() == 88888);

        ItemBrief[] bs = { b };
        String json = ModelCodec.from(bs).toJSON();
//        report(json);

        List<ItemBrief> list = ItemBrief.listFromJSON(json);
        assertFalse(list.isEmpty());
        assertTrue(list.get(0).getKey() == 88888);
    }

    @Test public void contactCodec() {
        Contact sample =
        Contact.named(TestName)
            .with(Kind.HOME, MailAddress.from("1234 Main St", "Anytown", "CA", "94005"))
            .with(Kind.HOME, EmailAddress.from(TestEmail))
            .with(Kind.HOME, PhoneNumber.from(TestPhone))
            ;

        String json = sample.toJSON();
//        report(json);

        Contact test = Contact.fromJSON(json);
        assertTrue(hasOne(test));
        test.describe();

        Contact[] cs = { sample };
        json = ModelCodec.from(cs).toJSON();
//        report(json);
    }

    @Test public void addressCodec() {
        MailAddress sample = MailAddress.from(TestMail);
        String json = sample.toJSON();
//        report(json);
//        String xml = ModelCodec.from(sample).toXML();
//        report(xml);

        MailAddress test = MailAddress.fromJSON(json);
        assertTrue(hasOne(test));
        test.describe();

        ContactMechanism mech = ContactMechanism.with(Kind.HOME, sample);
        mech.describe();
//        report(mech.toJSON());
    }

    @Test public void samplePhone() {
        PhoneNumber sample = PhoneNumber.from(TestPhone);
        assertTrue(sample.formatNumber().equals(TestPhone));
        sample.describe();
//        String xml = ModelCodec.from(sample).toXML();
//        report(xml);
    }

} // ContentTest
