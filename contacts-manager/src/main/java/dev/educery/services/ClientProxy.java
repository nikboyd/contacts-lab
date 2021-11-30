package dev.educery.services;

import dev.educery.codecs.ModelCodec;
import java.util.*;
import java.util.function.Function;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import dev.educery.domain.*;
import dev.educery.domain.Contact.Type;
import dev.educery.facets.IContactService;
import dev.educery.utils.Logging;
import static dev.educery.utils.Utils.wrap;

/**
 * A client-side service proxy for wrapping IContactService for tests.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@SuppressWarnings("unchecked")
public class ClientProxy implements Logging {

    protected IContactService contacts() { return this.proxy; }
    private final IContactService proxy = IContactService.loadProxy();

    protected Response r;
    protected String readJSON() { return r.readEntity(String.class); }
    public <R> R nullOr(Function<String, ? extends R> f, int code) {
        return (r.getStatus() == code) ? f.apply(readJSON()) : null; }

    public <R> List<R> emptyOr(Function<String, List<R>> f, int code) {
        return (r.getStatus() == code) ? f.apply(readJSON()) : new ArrayList(); }

    static final String ActualStatus = "actual response: %d";
    protected String actualStatus(Response r) { return String.format(ActualStatus, r.getStatus()); }

    static final int OK = Status.OK.getStatusCode();
    public ItemBrief countContacts() {
        r = contacts().countItems();
        return nullOr((json) -> ItemBrief.fromJSON(json), OK); }

    static final int CREATED = Status.CREATED.getStatusCode();
    public ItemBrief createContact(Contact c) {
        r = contacts().createItem(c.toJSON());
        return nullOr((json) -> ItemBrief.fromJSON(json), CREATED); }

    public ItemBrief saveContact(Contact c) {
        if (c.getKey() == 0) return createContact(c);
        r = contacts().saveItem(c.toJSON());
        return nullOr((json) -> ItemBrief.fromJSON(json), OK); }

    public List<String> checkContact(Contact c) {
        r = contacts().checkParts(c.toJSON());
        return (r.getStatus() == OK) ? new ArrayList() :
            wrap(ModelCodec.to(String[].class).fromJSON(readJSON())); }

    public Contact findFirst() {
        r = contacts().findFirstContact();
        return (r.getStatus() == OK) ? Contact.fromJSON(readJSON()) : null; }

    public ItemBrief savePart(ItemPart p) {
        r = contacts().createPart(p.toJSON());
        return nullOr((json) -> ItemBrief.fromJSON(json), CREATED); }

    public Contact getContact(Long id) {
        r = contacts().getItem(id);
        return nullOr((json) -> Contact.fromJSON(json), OK); }

    public List<Contact> findContact(PhoneNumber p) {
        r = contacts().getItemWithHash(Type.phone, p.formatValue());
        return emptyOr((json) -> Contact.listFromJSON(json), OK); }

    public List<Contact> findContact(EmailAddress a) {
        r = contacts().getItemWithHash(Type.email, a.formatValue());
        return emptyOr((json) -> Contact.listFromJSON(json), OK); }

    public List<Contact> findContact(String name) {
        r = contacts().getItemWithHash(Type.name, name);
        return emptyOr((json) -> Contact.listFromJSON(json), OK); }

    public List<Contact> listContactsLike(String name) {
        r = contacts().listItems(name, "", ""); // provisional
        return emptyOr((json) -> Contact.listFromJSON(json), OK); }

    public List<ItemBrief> listAllContactBriefs() {
        r = contacts().listBriefs("");
        return emptyOr((json) -> ItemBrief.listFromJSON(json), OK); }

    static final int ACCEPTED = Status.ACCEPTED.getStatusCode();
    public boolean deleteContact(Long key) {
        r = contacts().deleteItem(key);
        return (r.getStatus() == OK || r.getStatus() == ACCEPTED); }

} // ClientProxy
