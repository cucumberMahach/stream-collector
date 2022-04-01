package admin.stages;

import javafx.stage.Stage;
import util.StageUtil;

import java.io.IOException;
import java.net.URL;

public class BanStage extends Stage {

    public BanStage() {
        super();

        try {
            StageUtil.loadFXML(new URL("stages/BanStage.fxml"), this, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
