package bot;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotAlgorithm {
    void onUpdate(Update update);
    void setBot(Bot bot);
}
