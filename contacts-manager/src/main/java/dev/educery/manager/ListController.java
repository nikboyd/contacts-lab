package dev.educery.manager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import dev.educery.domain.Contact;
import dev.educery.services.ClientProxy;
import static dev.educery.utils.Utils.*;

/**
 * Manages a list of contact views.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ListController {

    @FXML Label messageArea;
    public void clearMessage() { setMessage(""); }
    public void setMessage(String message) { messageArea.setText(message); }

    @FXML ComboBox listSection;
    private String getSelectedSection() { return listSection.getValue().toString(); }
    private void selectSection(Contact c) { listSection.setValue(getSection(c)); }
    private String getSection(Contact c) { return c == null ? "A" : c.getName().substring(0, 1); }
    public void refreshContacts(Contact... cs) {
        if (hasSome(cs)) selectSection(cs[0]); refreshList();
        if (!hasSome(cs)) clearMessage(); }

    @FXML ComboBox showID;
    private void selectShown(String s) { showID.setValue(s); }
    public String getSelectedID() { return showID.getValue().toString(); }

    @FXML ListView<ContactView> contactList;
    private boolean hasSelection() { return contactList.getSelectionModel().getSelectedItem() != null; }
    private ContactView getSelectedView() { return contactList.getSelectionModel().getSelectedItem(); }
    private void refreshList() { contactList.setItems((ContactView.observeViews(getSelectedSection()))); }
    private void editSelectedContact() { if (hasSelection()) editContact(getSelectedView().getContact()); }
    private void editContact(Contact c) { ContactDialog.showFormDialog(c); }

    static final Object[] ShownIDs = {
        "HOME phone", "WORK phone", "MOBILE phone",
        "HOME email", "WORK email", "ONLY name"
    };
    static final Object[] Sections = {
        "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R",
        "S", "T", "U", "V", "W", "X", "Y", "Z",
    };
    @FXML public void initialize() {
        showID.getItems().addAll(ShownIDs);
        listSection.getItems().addAll(Sections);

        selectShown(ShownIDs[5].toString());
        selectSection(null); // initial section "A" ??
        refreshContacts(Contacts.findFirst());

        // attach handlers only after initializations
        contactList.setOnMouseClicked(mouseEvent -> editSelectedContact());
        showID.valueProperty().addListener((ov, ignored, text) -> { refreshContacts(); });
        listSection.valueProperty().addListener((ov, ignored, text) -> { refreshContacts(); });
    }

    static final ListController[] Active = { null };
    static public ListController activeController() { return Active[0]; }

    static final String ContactList = "contact-list";
    static public BorderPane loadView() { return ViewLoader.loadView(ContactList, Active); }

    static final ClientProxy Contacts = new ClientProxy();

} // ListController
