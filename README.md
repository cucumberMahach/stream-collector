# Stream Collector
Серверное приложение для сбора информации о пользователях с стримерских площадок (Twitch, Trovo, WASD, GoodGame)
с последующим сохранением в базу данных и отображением с помощью пользовательских запросов через Telegram бота.

*Автор: Александр Махоткин*

## О проекте
Программа собирает открытую информацию о посещениях пользователей стриминговых платформ (никах, время нахождения на стриме, 
одновременный просмотр каналов разных стримеров, ролях на канале). Полученную информацию сохраняет в базу данных. Из базы данных
данные отображаются в Telegram боте по запросам пользователей.

## Использованные технологии
В качестве языка программирования использовалась Java 17 с системой сборки Maven.
Была использована база данных MariaDB.

Зависимости проекта:
- JColor (5.2.0);
- gson (2.9.0);
- mariadb-java-client (3.0.4);
- hibernate-core (5.6.8.Final);
- hibernate-jcache (5.6.8.Final);
- hibernate-c3p0 (5.6.7.Final);
- ehcache (3.9.9);
- c3p0 (0.9.5.5);
- telegrambots (6.1.0);
- javafx (18);
- Java-WebSocket (1.5.3).

## Запуск
Программа тестировалась на следующих операционных системах:
- Windows 10 Pro;
- Ubuntu 20.04 LTS (только консольный интерфейс).

Для начала работы программы необходимо её настроить. Настройки программы хранятся в ресурсах (src/main/resources).
Настройки могут быть публичные (resources/settings/settings.json) и приватные (resources/private/private.json).

Структура публичных настроек:
```
{
    "circles_database": "Local",
    "bot_database": "Local",
    "bot_test_account": true,
    "admin_database": "Local",
    "donations_database": "Local",
    "remover_enabled": true,
    "remover_optimize": true
}
```

- `circles_database`, `bot_database`, `admin_database`, `donations_database` - настройки для какой базы данных загружать приложению. Возможные значения: **Local** (hibernate.cfg.xml),
  **Remote** (hibernate.remote.cfg.xml), **LocalOnServer** (hibernate.local.german.cfg.xml). Благодаря этой настройке возможно использование приложения с разных устройств;
- `bot_test_account` - использовать тестовый аккаунт Telegram бота;
- `remover_enabled`, `remover_optimize` - удаление старых записей в базе данных.

Структура приватных настроек:
```
{
    "telegram_token_test": "",
    "telegram_token": "",
    "trovo_client_id": "",
    "donat_bearer": ""
}
```

- `telegram_token_test` - токен Telegram бота для тестирования приложения;
- `telegram_token` - токен Telegram бота для релиза;
- `trovo_client_id` - используется для сбора информации с платформы Trovo. Выдается при создании приложения;
- `donat_bearer` - токен DonationAlerts для получения донатов и зачислении их пользователям.

После настройки приложения и изменения конфигурационных файлов базы данных (hibernate.*.xml) необходимо развернуть базу данных MariaDB.
Модель базы данных находится в файле db_model.mwb (открывается в MySQL Workbench 8.0). Далее производится экспорт модели в SQL скрипт и устранение различий синтаксиса
MySQL и MariaDB (готовый скрипт базы данных - db_script.sql).

Также необходимо создать функции в базе данных (_files/queries/func *.sql). В итоге БД должна иметь следующую структуру:

![](/_assets/screen_db.png "Screenshot DB")

Запуск приложения:
```
java -jar stream-collector-2.0-jar-with-dependencies.jar
```

Функционал приложения разделен на следующие микросервисы:
- **circle** - сбор информации с платформ;
- **bot** - Telegram бот;
- **admin** - панель администратора. Примечание: панель администратора требует графического интерфейса пользователя;
- **donations** - сбор донатов и зачисление их конкретным пользователям на счет.

Для запуска сервиса используется команда `start [service name]`. Далее можно открыть лог сервиса командой `log [service name]`.
Возможно объединение нескольких команд специальным оператором (пример для сервиса circle): `start circle | log circle`.
Для выхода из лога необходимо нажать клавишу `Enter`.

Все возможные команды можно посмотреть, набрав в консоли `help`.

## Скриншоты
![](/_assets/cli.png "Screenshot CLI")

![](/_assets/screen1.png "Screenshot 1")

![](/_assets/screen2.png "Screenshot 2")

![](/_assets/screen3.png "Screenshot 3")

![](/_assets/screen4.png "Screenshot 4")

![](/_assets/screen5.png "Screenshot 5")

