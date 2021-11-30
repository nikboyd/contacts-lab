package dev.educery.manager;

import java.util.List;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Alert.AlertType;

import dev.educery.domain.Contact;
import dev.educery.domain.ItemBrief;
import dev.educery.services.ClientProxy;
import static dev.educery.utils.Utils.*;
import dev.educery.utils.Logging;

/**
 * Manages the contact form dialog.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class ContactDialog implements Logging {

    static final ContactDialog DialogInstance = new ContactDialog();
    public static void showFormDialog(Contact... cs) { DialogInstance.showDialog(cs); }

    private Contact result;
    private Contact deleted;
    private Dialog<Contact> dialog;
    private void closeDialog(ActionEvent event) { dialog.close(); event.consume(); }

    private void buildDialog() {
        result = null;
        deleted = null;

        dialog = new Dialog();
        dialog.setTitle("Contact");
        GridPane p = FormController.loadView();
        dialog.getDialogPane().setContent(p);
    }

    private void buildDeleteButton(Contact... cs) {
        if (hasNo(cs)) return; // no contact to edit or delete
        ButtonType buttonTypeEsc = new ButtonType("Delete", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeEsc);
        dialog.getDialogPane().lookupButton(buttonTypeEsc)
            .addEventFilter(ActionEvent.ACTION, event -> signalDelete(event, cs[0]));
    }

    private void buildCancelButton() {
        ButtonType buttonTypeEsc = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeEsc);
        dialog.getDialogPane().lookupButton(buttonTypeEsc)
            .addEventFilter(ActionEvent.ACTION, event -> signalExit(event));
    }

    private void captureContact(Contact... cs) { result = activeForm().captureContact(cs); }
    private void buildSaveButton(Contact... cs) {
        ButtonType buttonTypeOk = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().lookupButton(buttonTypeOk)
            .addEventFilter(ActionEvent.ACTION, event -> captureContact(cs));
    }

    public void showDialog(Contact... cs) {
        buildDialog();
        buildDeleteButton(cs);
        buildCancelButton();
        buildSaveButton(cs);

        exitSignaled(false);
        clearContact();
        acceptContact(cs);
        cycleDialog();
        saveContact(cs);
    }

    private void cycleDialog() {
        while (hasNone(result)) {
            if (hasWarnings()) {
                showWarnings(activeForm().getMessages());
            }
            clearWarnings();
            dialog.showAndWait();
            if (exitSignaled()) {
                report("dialog exit after validation");
                return; // bail out now
            }
            checkContact();
        }
    }

    private void showWarnings(List<String> messages) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText("Validation Problems Detected");
        alert.setContentText(messages.toString());
        alert.showAndWait();
    }

    static final String SavedMessage = "saved '%s' key: %d";
    static final String AddedMessage = "added '%s' key: %d";
    private String formatSave(ItemBrief b, Contact... cs) {
        return format(hasSome(cs) ? SavedMessage : AddedMessage, result.getName(), b.getKey()); }

    static final String DeleteMessage = "deleted '%s' key: %d";
    private String formatDelete(Contact c) { return format(DeleteMessage, c.getName(), c.getKey()); }

    private void signalDelete(ActionEvent event, Contact c) {
        report("delete signaled");
        deleteContact(c);
        closeDialog(event);
    }

    private void signalExit(ActionEvent event) {
        report("dialog exit signaled");
        exitSignaled(true);
        closeDialog(event);
    }

    private boolean exitSignaled = false;
    private boolean exitSignaled() { return this.exitSignaled; }
    private void exitSignaled(boolean value) { this.exitSignaled = value; }

    private FormController activeForm() { return FormController.activeController(); }
    private ListController activeList() { return ListController.activeController(); }

    private void clearContact() { activeForm().clearContact(); }
    private void clearWarnings() { activeForm().clearMessages(); activeList().clearMessage(); }
    private boolean hasWarnings() { return activeForm().hasMessages(); }
    private void refreshContacts(Contact c) { activeList().refreshContacts(c); }
    private void acceptContact(Contact... cs) { if (hasSome(cs)) activeForm().acceptContact(cs[0]); }

    static final ClientProxy Contacts = new ClientProxy();
    private void checkContact() {
        if (hasOne(result)) {
            List<String> messages = Contacts.checkContact(result);
            if (!messages.isEmpty()) {
                showWarnings(messages);
                result = null;
            }
        }
    }

    private void saveContact(Contact... cs) {
        if (hasNone(result) || hasOne(deleted)) return;
        ItemBrief b = Contacts.saveContact(result);
        String message = formatSave(b, cs);
        activeList().setMessage(message);
        report(message);
        refreshContacts(result);
    }

    private void deleteContact(Contact c) {
        result = c;
        Contacts.deleteContact(result.getKey());
        String message = formatDelete(c);
        activeList().setMessage(message);
        report(message);
        deleted = c;
        refreshContacts(c);
    }

} // ContactDialog
