package dev.educery.domain;

import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import dev.educery.storage.*;
import dev.educery.domain.Contact.Kind;
import static dev.educery.utils.Utils.*;
import dev.educery.utils.Logging;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

/**
 * Confirms proper operation of sample models and their persistence.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(value = { "direct" })
@ContextConfiguration(classes = { PersistenceContext.class })
public class RepositoryTest implements Logging {

    @BeforeClass public static void prepare() { Logging.StaticLogger.report("started RepositoryTest"); }

    @Autowired StorageMechanism.Registry registry;
    public PhoneStorage phones() { return StorageMechanism.get(PhoneNumber.class); }

    @Test public void streamTest() {
        final EmbeddedStorageManager storageManager = EmbeddedStorage.start();
    }

    @Test public void registeredStores() { report("registered stores count = " + registry.size()); }

    @Test public void invalidAddress() {
        MailAddress a = MailAddress.with("1234 Main St", "Anytown", "CAA", "94005");
        String[] results = a.validate();
        assertTrue(results.length > 0);
        report(results[0]);
    }

    @Test public void validAddress() {
        MailAddress a = MailAddress.with("1234 Main St", "Anytown", "CA", "94005");
        String[] results = a.validate();
        assertTrue(results.length == 0);
    }

    @Test public void phoneSample() throws Exception {
        reportCount(PhoneNumber.count());

        PhoneNumber p = PhoneNumber.from("888-888-8888").saveItem();
        assertTrue(hasOne(p));
        reportPhone("saved", p);

        int count = 0;
        Iterable<PhoneNumber> it = phones().findAll();
        for (PhoneNumber px : it) count++;

        int testCount = PhoneNumber.count();
        assertTrue(testCount == count);
        reportCount(testCount);

        PhoneNumber n = p.findItem();
        assertTrue(hasOne(n));
        reportPhone("found", n);

        n = p.findWithHash();
        assertTrue(hasOne(n));
        reportPhone("found", n);

        n.removeItem();

        reportCount(PhoneNumber.count());
    }

    @Test public void phoneStability() {
        PhoneNumber n = PhoneNumber.from("888-888-8888").saveItem();
        PhoneNumber p = PhoneNumber.from("888-888-8888").saveItem();
        assertTrue(n.getKey() == p.getKey());
        assertTrue(n.hashKey() == p.hashKey());

        PhoneNumber x = PhoneNumber.from("888-888-8888").findWithHash();
        assertTrue(hasOne(x));
        assertTrue(x.getKey() == p.getKey());

        assertTrue(n.removeItem());
        assertTrue(hasNone(n.findItem()));
    }

    @Test public void addressStability() {
        MailAddress a = MailAddress.with("1234 Main St", "Anytown", "CA", "94005").saveItem();
        MailAddress b = MailAddress.with("1234 Main St", "Anytown", "CA", "94005").findWithHash();

        assertTrue(hasOne(b));
        assertTrue(b.getKey() == a.getKey());
        assertTrue(b.hashKey() == a.hashKey());
        a.describe();

        assertTrue(a.removeItem());
        assertTrue(hasNone(b.findItem()));
    }

    @Test public void componentLifecycle() {
        MailAddress a = MailAddress.with("1234 Main St", "Anytown", "CA", "94005").saveItem();

        assertTrue(hasOne(a));
        assertTrue(a.getKey() > 0);

        MailAddress b = a.findItem();
        assertTrue(hasOne(b));
        assertTrue(b.getKey() > 0);
        a.describe();

        b = b.withCity("Uptown").saveItem();
        assertTrue(hasOne(b));
        assertFalse(b.getKey() == a.getKey());
        assertFalse(b.hashKey() == a.hashKey());
        assertFalse(b.getCity().equals(a.getCity()));
        b.describe();

        assertTrue(b.removeItem());
        assertTrue(hasNone(b.findItem()));
    }

    @Transactional
    @Test public void compositeLifecycle() {
        reportCounts();

        PhoneNumber p = PhoneNumber.from("415-888-8899").saveItem();
        EmailAddress e = EmailAddress.from("sample@educery.dev").saveItem();

        MailAddress a = MailAddress.with("1234 Main St", "Anytown", "CA", "94005").saveItem();
        MailAddress b = MailAddress.with("4321 Main St", "Anytown", "CA", "94005").saveItem();

        Contact c = Contact.named("George Jungleman")
            .withAddress(Kind.HOME, a)
            .withAddress(Kind.WORK, b)
            .withEmail(Kind.HOME, e)
            .withPhone(Kind.HOME, p)
            .saveItem();

        assertTrue(hasOne(c));
        assertTrue(c.getKey() > 0);
        assertTrue(c.countPhones() > 0);
        assertTrue(c.countAddresses() > 0);

        reportCounts();

        Contact d = c.findItem();
        assertTrue(hasOne(d));
        assertTrue(d.getKey() > 0);
        assertTrue(d.countPhones() > 0);
        assertTrue(d.countAddresses() > 0);

        MailAddress w = b.findItem();
        assertTrue(hasOne(w));
        assertTrue(w.getKey() > 0);
        assertTrue(w.hashKey() == b.hashKey());

        w = d.getAddress(Kind.WORK);
        assertTrue(hasOne(w));
        assertTrue(w.getKey() > 0);
        assertTrue(w.getCity().equals(a.getCity()));

        PhoneNumber n = d.getPhone(Kind.HOME);
        assertTrue(hasOne(n));
        assertTrue(n.getKey() > 0);
        assertTrue(n.formatNumber().equals(p.formatNumber()));

        PhoneNumber pn = n.findItem();
        assertTrue(hasOne(pn));
        assertTrue(pn.getKey() > 0);
        assertTrue(pn.formatNumber().equals(p.formatNumber()));

        assertTrue(d.removeItem());
        assertTrue(hasNone(d.findItem()));

        assertTrue(a.removeItem());
        assertTrue(b.removeItem());
        assertTrue(p.removeItem());
        assertTrue(e.removeItem());

        reportCounts();
    }

//    @Ignore
    @Transactional
    @Test public void fullContact() {
        String testName = "George Jungleman";
        Contact sample = Contact.named(testName)
            .with(Kind.HOME, MailAddress.with("1234 Main St", "Anytown", "CA", "94005"))
            .with(Kind.HOME, EmailAddress.from("george@jungleman.com"))
            .with(Kind.HOME, PhoneNumber.from("415-888-8899"));

        Contact c = sample.saveItem();
        c.describe();

        Contact x = Contact.named(testName).findWithHash();
        assertTrue(x.getKey() == c.getKey());
        assertTrue(x.hashKey() == c.hashKey());

        List<Contact> cs = Contact.like("George");
        assertFalse(cs.isEmpty());

        c = Contact.findKey(c.getKey());
        assertTrue(hasOne(c));

        cs = Contact.findSimilar(EmailAddress.from("george@jungleman.com"));
        assertFalse(cs.isEmpty());

        cs = Contact.findSimilar(PhoneNumber.from("415-888-8899"));
        assertFalse(cs.isEmpty());

        assertTrue(x.removeItem());
        assertTrue(x.getAddress(Kind.HOME).removeItem());
        assertTrue(x.getEmail(Kind.HOME).removeItem());
        assertTrue(x.getPhone(Kind.HOME).removeItem());
    }

    static final String[] Names = { "George Jungleman", "George Jetson", "George Bungleman", };
    @Transactional
    @Test public void multipleContacts() {
        wrap(Names).forEach(n -> Contact.named(n).saveItem());
        List<Contact> cs = Contact.like("G");
        assertFalse(cs.isEmpty());
        cs.forEach(c -> c.describe());

        Contact x = Contact.findFirst();
        x.describe();
        assertTrue(x.getName().startsWith("G"));

        cs.forEach(c -> c.removeItem());
        x = Contact.findFirst();
        assertTrue(x == null);
    }

    static final String PhoneReport = "%s: %s";
    void reportPhone(String state, PhoneNumber n) { report(String.format(PhoneReport, state, n.hashDescription())); }

    void reportCount(int value) { report("count = " + value); }
    void reportCounts() {
        report("phone count = " + PhoneNumber.count());
        report("email count = " + EmailAddress.count());
        report("address count = " + MailAddress.count());
        report("contact count = " + Contact.count());
    }

} // RepositoryTest
