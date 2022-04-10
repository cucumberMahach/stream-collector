package com.twitchcollector.app.admin.stages.bans;

import com.twitchcollector.app.admin.AdminDatabase;
import com.twitchcollector.app.admin.stages.dataViews.TgBanView;
import com.twitchcollector.app.admin.stages.dataViews.TgUserView;
import com.twitchcollector.app.database.entities.TgBanEntity;
import com.twitchcollector.app.database.entities.TgUserEntity;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.util.converter.IntegerStringConverter;
import com.twitchcollector.app.util.TimeUtil;

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class BanStage implements Initializable {

    public Button btnUnban;
    public TextArea txtReason;
    public TextField search;
    public TableView<TgUserView> usersTable;
    public TableView<TgBanView> bansTable;
    public Button btnBan;
    public RadioButton rb_byTime;
    public ToggleGroup ban_time;
    public RadioButton rb_1day;
    public RadioButton rb_3days;
    public RadioButton rb_5days;
    public RadioButton rb_14days;
    public RadioButton rb_30days;
    public RadioButton rb_byDate;
    public RadioButton rb_forever;
    public Spinner<Integer> hours;
    public Spinner<Integer> minutes;
    public Spinner<Integer> seconds;
    public DatePicker date;
    public Label labBans;
    public ChoiceBox<String> cbShowCount;
    public Spinner<Integer> days;

    private final AdminDatabase database;
    private boolean hasActiveBan = false;
    private TgUserView currentUser;

    public BanStage() {
        database = new AdminDatabase();
    }

    private void searchUsers(String text){
        var showCount = cbShowCount.getSelectionModel().getSelectedItem();
        Integer count;
        if (showCount == null || showCount.equals("Все")){
            count = null;
        }else{
            count = Integer.valueOf(showCount);
        }

        ObservableList<TgUserView> data = usersTable.getItems();
        data.clear();
        var newData = database.searchUsers(text, count);
        data.addAll(newData);
    }

    private void getBans(TgUserEntity tgUser){
        ObservableList<TgBanView> data = bansTable.getItems();
        data.clear();
        var newData = database.getBans(tgUser, TimeUtil.getZonedNow());
        data.addAll(newData);
        Platform.runLater( () -> bansTable.scrollTo(bansTable.getItems().size()));
        labBans.setText(String.format("Кол-во банов: %d", data.size()));
        updateButtonsStates(newData);
    }

    private void updateButtonsStates(ArrayList<TgBanView> data){
        hasActiveBan = false;
        for (var line : data){
            if (!line.remain.get().isEmpty()){
                hasActiveBan = true;
                break;
            }
        }

        if (currentUser != null) {
            btnBan.setDisable(false);
            btnUnban.setDisable(!hasActiveBan);
        }else{
            btnBan.setDisable(true);
            btnUnban.setDisable(true);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UnaryOperator<TextFormatter.Change> numbersFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9]+") || newText.equals("0")) {
                return change;
            }
            return null;
        };

        days.getEditor().setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, numbersFilter));
        hours.getEditor().setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, numbersFilter));
        minutes.getEditor().setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, numbersFilter));
        seconds.getEditor().setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 0, numbersFilter));

        txtReason.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().length() <= 100 ? change : null));

        rb_byTime.setOnAction(actionEvent -> {
            days.setDisable(false);
            setEnableTime(true);
            setEnableDate(false);
        });
        rb_1day.setOnAction(actionEvent -> {
            days.setDisable(true);
            setEnableTime(true);
            setEnableDate(false);
        });
        rb_3days.setOnAction(actionEvent -> {
            days.setDisable(true);
            setEnableTime(true);
            setEnableDate(false);
        });
        rb_5days.setOnAction(actionEvent -> {
            days.setDisable(true);
            setEnableTime(true);
            setEnableDate(false);
        });
        rb_14days.setOnAction(actionEvent -> {
            days.setDisable(true);
            setEnableTime(true);
            setEnableDate(false);
        });
        rb_30days.setOnAction(actionEvent -> {
            days.setDisable(true);
            setEnableTime(true);
            setEnableDate(false);
        });
        rb_byDate.setOnAction(actionEvent -> {
            days.setDisable(false);
            setEnableTime(true);
            setEnableDate(true);
        });
        rb_forever.setOnAction(actionEvent -> {
            days.setDisable(true);
            setEnableTime(false);
            setEnableDate(false);
        });

        search.setOnAction(actionEvent -> {
            updateUsersTable();
        });

        usersTable.setOnMouseClicked(mouseEvent -> {
            currentUser = usersTable.getSelectionModel().getSelectedItem();
            if (currentUser == null)
                return;
            var tgUser = new TgUserEntity();
            tgUser.id = Long.parseUnsignedLong(currentUser.id.get());
            getBans(tgUser);
        });

        btnUnban.setOnAction(actionEvent -> {
            if (currentUser == null)
                return;
            var tgUser = new TgUserEntity();
            tgUser.id = Long.parseUnsignedLong(currentUser.id.get());
            if (!database.stopBan(tgUser, TimeUtil.getZonedNow())){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Ошибка при удалении бана");
                alert.setContentText("Возможно бана не существует или его срок действия подошёл к концу");
                alert.showAndWait();
            }
            getBans(tgUser);
        });

        btnBan.setOnAction(actionEvent -> {
            if (currentUser == null)
                return;
            var currentTime = TimeUtil.getZonedNow();

            var tgUser = new TgUserEntity();
            tgUser.id = Long.parseUnsignedLong(currentUser.id.get());

            var tgBan = new TgBanEntity();
            tgBan.tgUser = tgUser;
            tgBan.reason = txtReason.getText().strip();
            tgBan.fromTime = currentTime;
            tgBan.untilTime = getUntilTime(currentTime);

            if (!database.makeBan(tgBan)){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Подтверждение");
                alert.setHeaderText("Наличие активного бана");
                alert.setContentText("Пользователь имеет активный бан. Вы ходите закрыть активный бан и создать новый?");
                ButtonType yesButton = new ButtonType("Да", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType("Нет", ButtonBar.ButtonData.NO);
                alert.getButtonTypes().setAll(yesButton, noButton);
                alert.showAndWait().ifPresent(action -> {
                    if (action.getButtonData() == ButtonBar.ButtonData.YES) {
                        database.stopBan(tgUser, currentTime);
                        database.makeBan(tgBan);
                    }
                });
            }
            getBans(tgUser);
        });

        bansTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2){

                var banLine = bansTable.getSelectionModel().getSelectedItem();

                if (banLine != null) {
                    showNewReasonDialog(banLine, banLine.reason.get());
                }

            }
        });
    }

    private void updateUsersTable(){
        searchUsers(search.getText());
    }

    private void showNewReasonDialog(TgBanView tgBanView, String reason){
        TextInputDialog inputDialog = new TextInputDialog(reason);
        inputDialog.setTitle("Ввод причины бана");
        inputDialog.setHeaderText("Введите новую причину бана");
        inputDialog.setContentText("Причина:");
        inputDialog.showAndWait().ifPresent(s -> {
            if (s.length() > 100){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Некорректная длина причины");
                alert.setContentText("Длина причины должна быть не больше 100 символов");
                alert.showAndWait();
                showNewReasonDialog(tgBanView, s);
            }else {
                var tgBan = tgBanView.toTgBan();
                tgBan.reason = s;
                database.updateTgBan(tgBan);

                var tgUser = new TgUserEntity();
                tgUser.id = Long.parseUnsignedLong(currentUser.id.get());
                getBans(tgUser);
            }
        });
    }

    private ZonedDateTime getUntilTime(ZonedDateTime currentTime){
        int days = this.days.getValue();
        int hours = this.hours.getValue();
        int minutes = this.minutes.getValue();
        int seconds = this.seconds.getValue();
        ZonedDateTime untilTime = currentTime;

        if (rb_1day.isSelected()){
            days = 1;
        }else if (rb_3days.isSelected()){
            days = 3;
        }else if (rb_5days.isSelected()){
            days = 5;
        }else if (rb_14days.isSelected()){
            days = 14;
        }else if (rb_30days.isSelected()){
            days = 30;
        }else if (rb_byDate.isSelected()){
            untilTime = TimeUtil.fromLocalDate(date.getValue());
        }else if (rb_forever.isSelected()){
            untilTime = TimeUtil.maxZoned();
            hours = 0;
            minutes = 0;
            seconds = 0;
        }

        untilTime = untilTime.plusDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);

        if (untilTime.isAfter(TimeUtil.maxZoned())){
            untilTime = TimeUtil.maxZoned();
        }

        return untilTime;
    }

    private void setEnableTime(boolean enable){
        hours.setDisable(!enable);
        minutes.setDisable(!enable);
        seconds.setDisable(!enable);
    }

    private void setEnableDate(boolean enable){
        date.setDisable(!enable);
    }
}
