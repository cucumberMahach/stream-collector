package admin;


import admin.stages.BanStage;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import service.AdminService;
import util.StageUtil;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class AdminApp extends Application implements Initializable {

    private static AdminService service;

    @FXML
    private Button btnBans;

    public static void startApp(AdminService service){
        AdminApp.service = service;
        launch();
    }

    @Override
    public void start(Stage stage){
        try {
            StageUtil.loadFXML(new URL("resources/stages/MainStage.fxml"), stage, null, 0, 0, true, true);

            //new BanStage();
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
            CompletableFuture.runAsync(() -> {
                new BanStage();
            });
        });
    }
}
