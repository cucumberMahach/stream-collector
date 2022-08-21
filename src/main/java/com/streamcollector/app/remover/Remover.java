package com.streamcollector.app.remover;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.streamcollector.app.json.adapters.ZonedDateTimeAdapter;
import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.logging.Logger;
import com.streamcollector.app.service.AbstractService;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.util.TimeUtil;
import org.hibernate.StatelessSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;

public class Remover {

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter()).setPrettyPrinting().create();
    private static final String fileName = "remover.json";

    private final AbstractService logger;

    public Remover(AbstractService logger){
        this.logger = logger;
    }

    public void check(StatelessSession session){
        if (!Settings.instance.getSettings().removerEnabled)
            return;
        if (!Files.exists(Paths.get(fileName))){
            createFile();
        }else{
            var obj = readFile();
            if (TimeUtil.getZonedNow().isAfter(obj.nextRemoveDate)){
                forced(session);
            }
        }
    }

    public void forced(StatelessSession session){
        beginRemove(session);
        createFile();
    }

    private void createFile(){
        RemoveObject object = new RemoveObject();
        object.nextRemoveDate = TimeUtil.getZonedNow().plusDays(1).withHour(4).withMinute(0).withSecond(0).withNano(0);
        try {
            Files.writeString(Paths.get(fileName), gson.toJson(object));
            writeLog(LogStatus.Warning, "Следующая очистка запланирована на " + TimeUtil.formatZonedUser(object.nextRemoveDate));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RemoveObject readFile(){
        try {
            var str = Files.readString(Paths.get(fileName));
            return gson.fromJson(str, RemoveObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void beginRemove(StatelessSession session){
        writeLog(LogStatus.Warning, "Началась очистка БД");
        session.beginTransaction();
        var query = session.createNativeQuery("select removeUnnecessary(:cur_date)");
        query.setParameter("cur_date", TimeUtil.getZonedNow());
        query.getSingleResult();
        session.getTransaction().commit();
        writeLog(LogStatus.Success, "Очистка БД завершилась");

        if (!Settings.instance.getSettings().removerOptimize)
            return;

        writeLog(LogStatus.Warning, "Началась оптимизация таблиц");
        session.beginTransaction();
        query = session.createNativeQuery("optimize table channels_circles, users, users_channels");
        query.list();
        session.getTransaction().commit();
        writeLog(LogStatus.Success, "Оптимизация таблиц завершилась");
    }

    private void writeLog(LogStatus status, String message){
        if (logger == null){
            boolean globalLogEnabled = Logger.instance.isGlobalLogEnabled();
            Logger.instance.setGlobalLogEnabled(true);
            Logger.instance.writeLog(status, String.format("(remover) %s", message));
            Logger.instance.setGlobalLogEnabled(globalLogEnabled);
        }else{
            logger.writeLog(status, String.format("(remover) %s", message));
        }
    }
}
