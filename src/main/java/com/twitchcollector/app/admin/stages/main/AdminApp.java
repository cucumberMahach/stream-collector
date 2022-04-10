package com.twitchcollector.app.admin.stages.main;


import javafx.application.Application;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.twitchcollector.app.service.AdminService;
import com.twitchcollector.app.util.StageUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminApp extends Application implements Initializable {

    private static AdminService service;

    public Button btnQueries;
    public Button btnBans;

    public static void startApp(AdminService service){
        AdminApp.service = service;
        launch();
    }

    @Override
    public void start(Stage stage){
        try {
            stage.getIcons().add(new Image(AdminApp.class.getClassLoader().getResource("icons/admin.png").openStream()));
            StageUtil.loadFXML("stages/MainStage.fxml", stage, "Админ панель", 0, 0, true, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnBans.setOnAction(actionEvent -> {
            try {
                var stage = new Stage();
                stage.getIcons().add(new Image(AdminApp.class.getClassLoader().getResource("icons/police.png").openStream()));
                StageUtil.loadFXML("stages/Bans.fxml", stage, "Баны", 0, 0, false, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        btnQueries.setOnAction(actionEvent -> {
            try {
                var stage = new Stage();
                stage.getIcons().add(new Image(AdminApp.class.getClassLoader().getResource("icons/queries.png").openStream()));
            StageUtil.loadFXML("stages/Queries.fxml", stage, "Запросы", 0, 0, false, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
