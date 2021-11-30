package dev.educery.manager;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;

/**
 * Handles menu events.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class MenuController implements Initializable {

    @FXML MenuBar menuBar;
    @Override public void initialize(URL url, ResourceBundle rb) { menuBar.setFocusTraversable(true); }

    @FXML public void handleQuitAction(final ActionEvent ev) { Main.exit(); }
    @FXML public void handleNewAction(final ActionEvent ev) { ContactDialog.showFormDialog(); }
    @FXML public void handleAboutAction(final ActionEvent ev) { Main.showAboutDialog(); }

    static final ListController[] Active = { null };
    static public ListController activeController() { return Active[0]; }

    static final String ContactMenu = "contact-menu";
    static public VBox loadView() { return ViewLoader.loadView(ContactMenu); }

} // MenuController
