package dev.educery.services;

import java.util.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import static org.junit.Assert.*;

import dev.educery.domain.*;
import dev.educery.domain.Contact.Type;
import dev.educery.facets.IContactService;
import dev.educery.codecs.ModelCodec;
import static dev.educery.utils.Utils.*;
import dev.educery.utils.Logging;

/**
 * A client-side service proxy for wrapping IContactService for tests.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ClientProxy implements Logging {

    protected IContactService contacts() { return this.proxy; }
    private final IContactService proxy = IContactService.loadProxy();

    protected Response r;
    protected String readJSON() { return r.readEntity(String.class); }

    static final String ActualStatus = "actual response: %d";
    protected String actualStatus(Response r) { return String.format(ActualStatus, r.getStatus()); }

    static final int OK = Status.OK.getStatusCode();
    public ItemBrief countContacts() {
        r = contacts().countItems();
        assertTrue(actualStatus(r), r.getStatus() == OK);
        return ItemBrief.fromJSON(readJSON()); }

    static final int CREATED = Status.CREATED.getStatusCode();
    public ItemBrief createContact(Contact c) {
        r = contacts().createItem(c.toJSON());
        assertTrue(actualStatus(r), r.getStatus() == CREATED);
        return ItemBrief.fromJSON(readJSON()); }

    public ItemBrief saveContact(Contact c) {
        if (c.getKey() == 0) return createContact(c);
        r = contacts().saveItem(c.toJSON());
        assertTrue(actualStatus(r), r.getStatus() == OK);
        return ItemBrief.fromJSON(readJSON()); }

    public List<String> checkContact(Contact c) {
        r = contacts().checkParts(c.toJSON());
        return (r.getStatus() == OK) ? new ArrayList() :
            wrap(ModelCodec.to(String[].class).fromJSON(readJSON())); }

    public ItemBrief savePart(ItemPart p) {
        r = contacts().createPart(p.toJSON());
        assertTrue(actualStatus(r), r.getStatus() == CREATED);
        return ItemBrief.fromJSON(readJSON()); }

    public Contact findFirst() {
        r = contacts().findFirstContact();
        return (r.getStatus() == OK) ? Contact.fromJSON(readJSON()) : null; }

    public Contact getContact(Long id) {
        r = contacts().getItem(id);
        assertTrue(actualStatus(r), r.getStatus() == OK);
        Contact c = Contact.fromJSON(readJSON());
        assertTrue(hasOne(c));
        return c; }

    public List<Contact> findContact(PhoneNumber p) {
        r = contacts().getItemWithHash(Type.phone, p.formatValue());
        assertTrue(actualStatus(r), r.getStatus() == OK);
        List<Contact> results = Contact.listFromJSON(readJSON());
        assertFalse(results.isEmpty());
        return results; }

    public List<Contact> findContact(EmailAddress a) {
        r = contacts().getItemWithHash(Type.email, a.formatValue());
        assertTrue(actualStatus(r), r.getStatus() == OK);
        List<Contact> results = Contact.listFromJSON(readJSON());
        assertFalse(results.isEmpty());
        return results; }

    public List<Contact> findContact(String name) {
        r = contacts().getItemWithHash(Type.name, name);
        assertTrue(actualStatus(r), r.getStatus() == OK);
        List<Contact> results = Contact.listFromJSON(readJSON());
        assertFalse(results.isEmpty());
        return results; }

    public List<Contact> listContactsLike(String name) {
        r = contacts().listItems(name, "", ""); // provisional
        assertTrue(actualStatus(r), r.getStatus() == OK);
        List<Contact> results = Contact.listFromJSON(readJSON());
        assertFalse(results.isEmpty());
        return results; }

    public List<ItemBrief> listAllContactBriefs() {
        r = contacts().listBriefs("");
        assertTrue(actualStatus(r), r.getStatus() == OK);
        return ItemBrief.listFromJSON(readJSON()); }

    static final int ACCEPTED = Status.ACCEPTED.getStatusCode();
    public boolean deleteContact(Long key) {
        r = contacts().deleteItem(key);
        return (r.getStatus() == OK || r.getStatus() == ACCEPTED); }

} // ClientProxy
