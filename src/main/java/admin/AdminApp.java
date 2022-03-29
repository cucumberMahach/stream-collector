package admin;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import service.AdminService;

public class AdminApp extends Application {

    private static AdminService service;
    public static void startApp(AdminService service){
        AdminApp.service = service;
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Label label = new Label("Hello");               // текстовая метка
        Button button = new Button("Button");           // кнопка
        Group group = new Group(button);                // вложенный узел Group

        FlowPane root = new FlowPane(label, group);       // корневой узел
        Scene scene = new Scene(root, 300, 150);        // создание Scene
        stage.setScene(scene);                          // установка Scene для Stage

        stage.setTitle("Hello JavaFX");

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
