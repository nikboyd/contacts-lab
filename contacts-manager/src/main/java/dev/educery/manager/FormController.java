package dev.educery.manager;

import java.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import dev.educery.domain.*;
import dev.educery.domain.Contact.Kind;
import dev.educery.storage.Descriptive;
import dev.educery.utils.Logging;
import static dev.educery.utils.Utils.*;
import static dev.educery.utils.Logging.*;

/**
 * Manages contact display and capture.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class FormController implements Logging {

    @FXML TextField contactName;
    @FXML TextField homePhone;
    @FXML TextField workPhone;
    @FXML TextField mobilePhone;
    @FXML TextField homeEmail;
    @FXML TextField workEmail;
    @FXML TextField homeAddress;
    @FXML TextField workAddress;
    @FXML TextField billAddress;
    @FXML TextField shipAddress;

    private final List<String> messages = new ArrayList();
    public void clearMessages() { this.messages.clear(); }
    public List<String> getMessages() { return this.messages; }
    public boolean hasMessages() { return !getMessages().isEmpty(); }

    public Contact captureContact(Contact... cs) {
        if (hasSome(cs)) {
            item = cs[0];
            return updateContact();
        }
        else {
            return buildContact();
        }
    }

    public Contact buildContact() {
        if (!isEmpty(contactName.getText())) {
            item = Contact.named(contactName.getText());
            updatePhone(Kind.HOME, homePhone);
            updatePhone(Kind.WORK, workPhone);
            updatePhone(Kind.MOBILE, mobilePhone);
            updateEmail(Kind.HOME, homeEmail);
            updateEmail(Kind.WORK, workEmail);
            updateAddress(Kind.HOME, homeAddress);
            updateAddress(Kind.WORK, workAddress);
            updateAddress(Kind.BILLING, billAddress);
            updateAddress(Kind.SHIPPING, shipAddress);
        }
        return hasMessages() ? null : item;
    }

    public Contact updateContact() {
        updatePhone(Kind.HOME, homePhone);
        updatePhone(Kind.WORK, workPhone);
        updatePhone(Kind.MOBILE, mobilePhone);
        updateEmail(Kind.HOME, homeEmail);
        updateEmail(Kind.WORK, workEmail);
        updateAddress(Kind.HOME, homeAddress);
        updateAddress(Kind.WORK, workAddress);
        updateAddress(Kind.BILLING, billAddress);
        updateAddress(Kind.SHIPPING, shipAddress);
        return hasMessages() ? null : item;
    }

    private Contact item;
    public void clearContact() { this.item = null; }
    public void acceptContact(Contact c) {
        item = c;
        contactName.setText(item.getName());
        contactName.setEditable(false);
//        contactName.setDisable(true);

        homePhone.setText(getPhone(Kind.HOME));
        workPhone.setText(getPhone(Kind.WORK));
        mobilePhone.setText(getPhone(Kind.MOBILE));
        homeEmail.setText(getEmail(Kind.HOME));
        workEmail.setText(getEmail(Kind.WORK));
        homeAddress.setText(getAddress(Kind.HOME));
        workAddress.setText(getAddress(Kind.WORK));
        billAddress.setText(getAddress(Kind.BILLING));
        shipAddress.setText(getAddress(Kind.SHIPPING));
    }

    private String getPhone(Kind k) { return getValue(item.getPhone(k)); }
    private String getEmail(Kind k) { return getValue(item.getEmail(k)); }
    private String getAddress(Kind k) { return getValue(item.getAddress(k)); }
    private String getValue(Descriptive d) { return hasOne(d) ? d.formatValue() : ""; }

    private void updatePhone(Kind k, TextField text) { item.mergePhone(k, text.getText(), messages); }
    private void updateEmail(Kind k, TextField text) { item.mergeEmail(k, text.getText(), messages); }
    private void updateAddress(Kind k, TextField text) { item.mergeAddress(k, text.getText(), messages); }

    static final FormController[] Active = { null };
    static public FormController activeController() { return Active[0]; }

    static final String ContactForm = "contact-form";
    static public GridPane loadView() { return ViewLoader.loadView(ContactForm, Active); }

} // FormController
