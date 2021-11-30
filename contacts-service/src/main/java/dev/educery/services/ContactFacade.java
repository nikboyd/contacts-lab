package dev.educery.services;

import java.util.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.cxf.common.util.StringUtils;
import org.springframework.stereotype.Service;

import dev.educery.domain.*;
import dev.educery.storage.StorageMechanism;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import dev.educery.facets.IContactService;
import static dev.educery.utils.Utils.*;

/**
 * A service for maintaining Contacts and their Contact information.
 *
 * @author nik <nikboyd@sonic.net>
 */
@Service
@Transactional
@Path(IContactService.BasePath)
public class ContactFacade implements IContactService {

    @Autowired StorageMechanism.Registry registry;

    @Override public Response countItems() {
        int count = Contact.count();
        ItemBrief b = new ItemBrief(count, "Contact.count");
        return Response.ok(b.toJSON()).build();
    }

    @Override public Response findFirstContact() {
        Contact c = Contact.findFirst();
        return hasNone(c) ? Response.status(Status.GONE).build() : Response.ok(c).build(); }


    @Override public Response listBriefs(String name) {
        List<ItemBrief> results = new ArrayList();
        if (StringUtils.isEmpty(name)) {
            for (Contact c : Contact.storage().findAll()) results.add(c.brief());
        } else {
            List<Contact> items = Contact.like(name);
            items.forEach((c) -> results.add(c.brief()));
        }
        return Response.ok(results).build();
    }

    @Override public Response listItems(String name, String city, String zip) {
        List<Contact> results = Contact.like(name);
        return Response.ok(results).build();
    }

    @Override public Response checkParts(String itemJSON) {
        Contact item = Contact.fromJSON(itemJSON);
        if (hasNone(item)) return Response.status(Status.GONE).build();

        List<String> messages = Contact.checkParts(item);
        return messages.isEmpty() ? Response.ok(messages).build() :
            Response.ok(messages).status(Status.CONFLICT).build() ;
    }

    @Override public Response createItem(String itemJSON) {
        Contact item = Contact.fromJSON(itemJSON);
        List<String> messages = Contact.checkParts(item);
        if (messages.isEmpty()) {
            ItemBrief b = ItemBrief.from(item.saveItem());
            return Response.ok(b.toJSON()).status(Status.CREATED).build();
        }
        else {
            return Response.ok(messages).status(Status.CONFLICT).build();
        }
    }

    @Override public Response saveItem(String itemJSON) {
        Contact item = Contact.fromJSON(itemJSON);
        if (item.getKey() == 0) return Response.status(Status.CONFLICT).build();

        Contact c = Contact.findKey(item.getKey());
        if (hasNone(c)) return Response.status(Status.GONE).build();

        List<String> messages = Contact.checkParts(item);
        if (messages.isEmpty()) {
            c = item.saveItem();
            ItemBrief b = ItemBrief.from(c);
            return Response.ok(b.toJSON()).build();
        }
        else {
            return Response.ok(messages).status(Status.CONFLICT).build();
        }
    }

    @Override public Response getItem(long itemID) {
        Contact c = Contact.findKey(itemID);
        return hasNone(c) ? Response.status(Status.GONE).build() : Response.ok(c).build(); }

    @Override public Response getItemWithHash(Contact.Type idType, String itemID) {
        switch (idType) {
            case name:  return Response.ok(Contact.findNamed(itemID)).build();
            case email: return Response.ok(Contact.findSimilar(EmailAddress.from(itemID))).build();
            case phone: return Response.ok(Contact.findSimilar(PhoneNumber.from(itemID))).build();
        }
        Contact[] results = { }; return Response.ok(wrap(results)).build();
    }

    @Override public Response deleteItem(long itemID) {
        Contact c = Contact.findKey(itemID);
        if (hasNone(c)) return Response.accepted().build();
        boolean gone = c.removeItem();
        return Response.ok().build();
    }

    private Response deleteFirst(List<Contact> cs) {
        if (cs.isEmpty()) return Response.accepted().build();
        boolean gone = cs.get(0).removeItem(); return Response.ok().build(); }

    @Override public Response deleteItemWithHash(Contact.Type idType, String itemID) {
        switch (idType) {
            case name: return deleteFirst(Contact.findNamed(itemID));
            case email: return deleteFirst(Contact.findSimilar(EmailAddress.from(itemID)));
            case phone: return deleteFirst(Contact.findSimilar(PhoneNumber.from(itemID)));
        }
        return Response.accepted().build();
    }

    @Override public Response createPart(String partJSON) {
        ItemPart p = ItemPart.fromJSON(partJSON);
        switch (p.type()) {
            case name:  return createItem(p);
            case phone: return createPhone(p);
            case email: return createEmail(p);
            case mail:  return createMail(p);
        }
        ItemBrief b = new ItemBrief(0, "invalid part request");
        return Response.ok(b.toJSON()).status(Status.CONFLICT).build();
    }

    private Response createItem(ItemPart p) {
        Contact c = Contact.named(p.getName()).saveItem();
        ItemBrief b = ItemBrief.from(c);
        return Response.ok(b.toJSON()).status(Status.CREATED).build();
    }

    private Response createPhone(ItemPart p) {
        Contact c = Contact.find(p.getName());
        if (hasNone(c)) return Response.status(Status.GONE).build();

        PhoneNumber item = PhoneNumber.from(p.value());
        c.withPhone(p.kind(), item).saveItem();
        c = Contact.storage().findKey(c.getKey());
        ItemBrief b = ItemBrief.from(c.getPhone(p.kind()));
        return Response.ok(b.toJSON()).status(Status.CREATED).build();
    }

    private Response createEmail(ItemPart p) {
        Contact c = Contact.find(p.getName());
        if (hasNone(c)) return Response.status(Status.GONE).build();

        EmailAddress item = EmailAddress.from(p.value());
        c.withEmail(p.kind(), item).saveItem();
        c = Contact.storage().findKey(c.getKey());
        ItemBrief b = ItemBrief.from(c.getEmail(p.kind()));
        return Response.ok(b.toJSON()).status(Status.CREATED).build();
    }

    private Response createMail(ItemPart p) {
        Contact c = Contact.find(p.getName());
        long hash = c.hashKey();
        if (hasNone(c)) return Response.status(Status.GONE).build();

        MailAddress item = MailAddress.from(p.value());
        c.withAddress(p.kind(), item).saveItem();
        c = Contact.storage().findKey(c.getKey());
        ItemBrief b = ItemBrief.from(c.getAddress(p.kind()));
        return Response.ok(b.toJSON()).status(Status.CREATED).build();
    }

} // ContactFacade
