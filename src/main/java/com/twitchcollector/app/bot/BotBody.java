package com.twitchcollector.app.bot;

import com.twitchcollector.app.bot.logic.BotLogic;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.twitchcollector.app.util.TimeUtil;

import java.time.ZonedDateTime;

public abstract class BotBody {
    protected ZonedDateTime currentTime;
    protected Bot bot;
    protected BotLogic logic;

    protected void initCurrentTime(){
        currentTime = TimeUtil.getZonedNow();
    }

    public abstract void onUpdate(Update update);

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public Bot getBot() {
        return bot;
    }

    public ZonedDateTime getCurrentTime() {
        return currentTime;
    }

    public BotLogic getLogic() {
        return logic;
    }

    public void setLogic(BotLogic logic) {
        this.logic = logic;
        logic.setBotBody(this);
    }
}
