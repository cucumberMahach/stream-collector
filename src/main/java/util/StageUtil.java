package util;

import admin.AdminApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class StageUtil {
    public static void loadFXML(URL filename, Stage stage, String title, double width, double height, boolean alwaysOnTop, boolean show) throws IOException {
        Parent root = FXMLLoader.load(filename);
        Scene scene = new Scene(root);

        stage.setScene(scene);

        if (title != null)
            stage.setTitle(title);
        if (width != 0)
            stage.setWidth(width);
        if (height != 0)
        stage.setHeight(height);
        stage.setAlwaysOnTop(alwaysOnTop);

        if (show)
            stage.show();
    }

    public static void loadFXML(URL filename, Stage stage, boolean show) throws IOException {
        loadFXML(filename, stage, null, 0, 0, false, show);
    }
}
