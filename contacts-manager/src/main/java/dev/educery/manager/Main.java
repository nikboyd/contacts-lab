package dev.educery.manager;

import dev.educery.app.Servant;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.application.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

/**
 * JavaFX Main.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Main extends Application {

    static { if (!checkServer()) { startServer(); awaitServer(); }}
    static public void exit() { stopServer(); Platform.exit(); }
    static private void stopServer() { Servant.stopServer();}
    static private void startServer() { Servant.startServer(); }
    static private void awaitServer() { Servant.waitForServer(); }
    static private boolean checkServer() { return Servant.checkServer(); }

    private Stage rootStage;
    @Override public void start(Stage stage) {
        rootStage = stage;
        rootStage.setTitle(MainWindowTitle);

        showRootPane();
        showMenuBar();
    }

    private BorderPane rootPane;
    private static Scene MainScene;
    private void showRootPane() {
        rootPane = ListController.loadView();
        MainScene = new Scene(rootPane, 450, 450);
        rootStage.setScene(MainScene);
        rootStage.show();
    }

    private void showMenuBar() {
        VBox barView = MenuController.loadView();
        rootPane.setTop(barView);
        MainScene.getRoot().setStyle(FontStyle);
    }

    static final String FontStyle = "-fx-font-family: 'serif'";
    static final String MainWindowTitle = "Contact Manager";

    static final String AboutText = "Contact Manager v2021.1001.1001,\nCopyright 2010,2021 Nikolas S Boyd.";
    public static void showAboutDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About Contact Manager");
        alert.setContentText(AboutText);
        alert.showAndWait();
    }

} // Main