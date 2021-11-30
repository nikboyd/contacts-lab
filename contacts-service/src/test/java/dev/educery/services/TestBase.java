package dev.educery.services;

import org.junit.*;
import org.springframework.context.ConfigurableApplicationContext;

import dev.educery.domain.*;
import dev.educery.domain.Contact.Kind;
import dev.educery.server.MainController;
import dev.educery.utils.Logging;

/**
 * A base class for service tests.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class TestBase implements Logging {

    @Before public void startServer() { context = MainController.startApplication(); }
    @After  public void stopServer() { context.close(); }
    private ConfigurableApplicationContext context;

    private final ClientProxy clientProxy = new ClientProxy();
    protected ClientProxy clientProxy() { return this.clientProxy; }

    static final String TestEmail = "george@jungleman.com";
    protected EmailAddress createSampleEmail() { return createSimpleEmail(TestEmail); }
    protected EmailAddress createSimpleEmail(String m) { return EmailAddress.from(m); }

    static final String TestPhone = "415-888-8899";
    protected PhoneNumber createSamplePhone() { return createSimplePhone(TestPhone); }
    protected PhoneNumber createSimplePhone(String p) { return PhoneNumber.from(p); }

    protected MailAddress createSimpleAddress(String a) { return MailAddress.from(a); }
    protected MailAddress createSampleAddress(String streetAddress) {
        return MailAddress.with(streetAddress, "Anytown", "CA", "94005"); }

    protected Contact createSampleContact() {
        return Contact.named("George Jungleman")
            .with(Kind.HOME, createSampleAddress("1234 Main St"))
            .with(Kind.HOME, createSampleEmail())
            .with(Kind.HOME, createSamplePhone()); }

    protected Contact createSimpleContact(String name, String phone, String street) {
        return Contact.named(name)
            .with(Kind.HOME, createSampleAddress(street))
            .with(Kind.HOME, createSimplePhone(phone)); }

} // TestBase
