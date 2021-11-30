package dev.educery.manager;

import java.util.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import dev.educery.domain.*;
import dev.educery.domain.Contact.Kind;
import dev.educery.services.ClientProxy;
import dev.educery.utils.Logging;

/**
 * A contact view.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ContactView implements Logging {

    private final Contact contact;
    public ContactView(Contact c) { this.contact = c; name.set(c.getName()); }
    public Contact getContact() { return this.contact; }

    public String getName() { return name.get(); }
    public void setName(String n) { name.set(n); }

    static final String ViewForm = "%s    %s";
    @Override public String toString() {
        if (selectedID().endsWith(Name)) return getName();
        return format(ViewForm, getName(), formatSelectedID()); }

    private final StringProperty name = new SimpleStringProperty();
    public StringProperty nameProperty() { return name; }

    public static ObservableList observeViews(String start) { return wrapViews(collectViews(start)); }
    private static ObservableList wrapViews(List<ContactView> vs) { return FXCollections.observableArrayList(vs); }

    static final ClientProxy Contacts = new ClientProxy();
    public static List<ContactView> collectViews(String start) {
        List<Contact> cs = Contacts.listContactsLike(start);
        List<ContactView> results = new ArrayList();
        cs.forEach((c) -> { results.add(new ContactView(c)); });
        return results;
    }

    private ListController activeList() { return ListController.activeController(); }
    private String selectedID() { return activeList().getSelectedID(); }

    static final String Blank = " ";
    static final String Name = "name";
    static final String Phone = "phone";
    static final String Email = "email";
    static final String Unknown = "???";
    private String formatSelectedID() {
        String[] parts = selectedID().split(Blank);
        Kind k = Kind.valueOf(parts[0]);
        if (parts[1].equals(Phone)) {
            return contact.hasPhone(k) ? contact.getPhone(k).formatValue() : Unknown;
        }
        if (parts[1].equals(Email)) {
            return contact.hasEmail(k) ? contact.getEmail(k).formatValue() : Unknown;
        }
        return Empty;
    }

} // ContactView
