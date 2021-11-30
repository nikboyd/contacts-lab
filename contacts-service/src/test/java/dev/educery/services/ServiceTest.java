package dev.educery.services;

import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

import dev.educery.domain.*;
import dev.educery.domain.Contact.Kind;
import static dev.educery.utils.Utils.*;

/**
 * Confirms proper operation of the contact service.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ServiceTest extends TestBase {

    @Test public void createContactParts() {
        // ensure clean backing store
        ItemBrief b = clientProxy().countContacts();
        if (b.getKey() > 0) deleteSamples();

        // create contact from part
        String testName = "George Bungleman";
        ItemPart p = ItemPart.contact(testName);
        ItemBrief cb = clientProxy().savePart(p);
        assertTrue(cb.getType().contains(testName));

        // add home phone to contact from part
        p = ItemPart.contact(testName).with(Kind.HOME, createSamplePhone());
        ItemBrief pb = clientProxy().savePart(p);
        assertTrue(pb.getType().contains(TestPhone));

        // add home address to contact from part
        p = ItemPart.contact(testName).with(Kind.HOME, createSampleAddress("4321 Main St"));
        pb = clientProxy().savePart(p);
        assertTrue(pb.getType().contains("Anytown"));

        // fetch contact and confirm home phone and address
        Contact c = clientProxy().getContact(cb.getKey());
        assertTrue(c.getPhone(Kind.HOME).formatValue().equals(TestPhone));
        assertTrue(c.getAddress(Kind.HOME).formatValue().contains("Anytown"));
    }

    @Test public void createSamples() {
        // ensure clean backing store
        ItemBrief b = clientProxy().countContacts();
        if (b.getKey() > 0) deleteSamples();

        // create contact directly
        Contact c = createSampleContact();
        b = clientProxy().saveContact(c);
        c = clientProxy().getContact(b.getKey());
        c.describe();

        b = clientProxy().countContacts();
        report(b.toString());

        // find contact using its home phone
        List<Contact> results = clientProxy().findContact(c.getPhone(Kind.HOME));
        assertTrue(c.getName().equals(results.get(0).getName()));

        // find contact using its home email
        results = clientProxy().findContact(c.getEmail(Kind.HOME));
        assertTrue(c.getName().equals(results.get(0).getName()));

        // find contact using its name
        results = clientProxy().findContact(c.getName());
        assertTrue(c.getName().equals(results.get(0).getName()));

        // replace contact home address
        MailAddress a = c.getAddress(Kind.HOME);
        c.withAddress(Kind.HOME, a.withCity("Sometown"));
        b = clientProxy().saveContact(c);

        // fetch contact and confirm home address changed
        c = clientProxy().getContact(b.getKey());
        assertTrue(c.getAddress(Kind.HOME).getCity().equals("Sometown"));
        c.describe();

        // add another contact
        String testName = "George Bungleman";
        Contact simple = createSimpleContact(testName, "415-889-9988", "4321 Main St");
        b = clientProxy().saveContact(simple);
        c = clientProxy().getContact(b.getKey());
        assertTrue(c.getName().equals(testName));

        // confirm contacts exist
        results = clientProxy().listContactsLike("George");
        assertFalse(results.isEmpty());
        report("found " + results.size() + " matches");

        // check duplicate
        Contact dupe = createSimpleContact(testName, "415-889-9988", "888 Main St");
        List<String> messages = clientProxy().checkContact(dupe);
        report(messages.toString());

        // fetch a list of contact briefs
        List<ItemBrief> bs = clientProxy().listAllContactBriefs();
        assertFalse(bs.isEmpty());
        report("found " + bs.size() + " briefs");
    }

    /**
     * To retain samples created above, ignore this test method.
     * Then, launch the web service from the contacts-service folder.
     */
//    @Ignore
    @Test public void deleteSamples() {
        List<ItemBrief> bs = clientProxy().listAllContactBriefs();
        if (hasSome(bs)) {
            bs.forEach((b) -> {
                assertTrue(clientProxy().deleteContact(b.getKey()));
            });

            assertTrue(clientProxy().deleteContact(bs.get(0).getKey()));
        }

        bs = clientProxy().listAllContactBriefs();
        assertTrue(bs.isEmpty());
    }

} // ServiceTest
