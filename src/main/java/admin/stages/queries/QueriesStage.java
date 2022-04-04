package admin.stages.queries;

import admin.AdminDatabase;
import admin.stages.dataViews.TgHistoryView;
import admin.stages.dataViews.TgUserView;
import database.entities.TgUserEntity;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import util.TimeUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;

public class QueriesStage implements Initializable {

    private final AdminDatabase database;
    public TextField searchUsers;
    public ChoiceBox<String> cbShowCountUsers;
    public TableView<TgUserView> usersTable;
    public TableView<TgHistoryView> historyTable;
    public TextField searchHistory;
    public CheckBox checkGlobalHistory;
    public ChoiceBox<String> cbShowCountHistory;
    public WebView infoWeb;
    public DatePicker historyDate;
    public CheckBox checkHistoryDate;

    public QueriesStage(){
        database = new AdminDatabase();
    }

    private void searchUsers(String text){
        var showCount = cbShowCountUsers.getSelectionModel().getSelectedItem();
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

    private void searchHistory(TgUserEntity tgUser, String text, ZonedDateTime date){
        var showCount = cbShowCountHistory.getSelectionModel().getSelectedItem();
        Integer count;
        if (showCount == null || showCount.equals("Все")){
            count = null;
        }else{
            count = Integer.valueOf(showCount);
        }

        ObservableList<TgHistoryView> data = historyTable.getItems();
        data.clear();
        var newData = database.searchHistory(tgUser, text, count, date);
        data.addAll(newData);

        Platform.runLater( () -> historyTable.scrollTo(historyTable.getItems().size()));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        checkHistoryDate.setOnAction(actionEvent -> {
            historyDate.setDisable(!checkHistoryDate.isSelected());
            updateHistoryTable();
        });
        historyDate.setOnAction(actionEvent -> {
            updateHistoryTable();
        });
        checkGlobalHistory.setOnAction(actionEvent -> {
            updateHistoryTable();
        });
        cbShowCountUsers.setOnAction(actionEvent -> {
            updateUsersTable();
        });
        cbShowCountHistory.setOnAction(actionEvent -> {
            updateHistoryTable();
        });

        searchUsers.setOnAction(actionEvent -> {
            updateUsersTable();
        });
        searchHistory.setOnAction(actionEvent -> {
            updateHistoryTable();
        });
        usersTable.setOnMouseClicked(mouseEvent -> {
            updateHistoryTable();
        });
        historyTable.setOnMouseClicked(mouseEvent -> {
            var currentHistory = historyTable.getSelectionModel().getSelectedItem();
            if (currentHistory == null)
                return;
            showHtmlInfo(currentHistory);
        });
    }

    private void showHtmlInfo(TgHistoryView tgHistoryView){
        try {
            var engine = infoWeb.getEngine();
            var html = new String(QueriesStage.class.getClassLoader().getResourceAsStream("html/history.html").readAllBytes(), StandardCharsets.UTF_8);
            html = html.replaceAll("%history_id%", tgHistoryView.history_id.get());
            html = html.replaceAll("%user_id%", tgHistoryView.user_id.get());
            html = html.replaceAll("%message%", tgHistoryView.message.get());
            html = html.replaceAll("%result%", tgHistoryView.result.get());
            html = html.replaceAll("%messageTime%", tgHistoryView.messageTime.get());
            html = html.replaceAll("%requestTime%", tgHistoryView.requestTime.get());
            html = html.replaceAll("%answerTime%", tgHistoryView.answerTime.get());
            html = html.replaceAll("%duration%", tgHistoryView.duration.get());
            engine.loadContent(html);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateHistoryTable(){
        var currentUser = usersTable.getSelectionModel().getSelectedItem();
        var date = historyDate.getValue() == null || !checkHistoryDate.isSelected() ? null : TimeUtil.fromLocalDate(historyDate.getValue());
        if (currentUser == null || checkGlobalHistory.isSelected()){
            searchHistory(null, searchHistory.getText(), date);
        }else {
            var tgUser = new TgUserEntity();
            tgUser.id = Long.parseUnsignedLong(currentUser.id.get());
            searchHistory(tgUser, searchHistory.getText(), date);
        }
    }

    private void updateUsersTable(){
        searchUsers(searchUsers.getText());
    }
}
