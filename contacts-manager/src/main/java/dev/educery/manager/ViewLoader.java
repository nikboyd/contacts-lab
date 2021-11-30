package dev.educery.manager;

import java.io.*;
import java.net.*;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

/**
 *
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class ViewLoader {

    static public <T extends Parent> T loadView(String viewName) {
        try {
            return buildLoader(viewName).load();
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    static public <T extends Parent> T loadView(String viewName, Object[] controller) {
        try {
            FXMLLoader loader = buildLoader(viewName);
            T pane =  loader.load();
            controller[0] = loader.getController();
            return pane;
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    static FXMLLoader buildLoader(String fileName) throws IOException { return new FXMLLoader(findResource(fileName)); }
    static URL findResource(String fileName) { return Main.class.getResource(fileName + ".fxml"); }

} // ViewLoader
