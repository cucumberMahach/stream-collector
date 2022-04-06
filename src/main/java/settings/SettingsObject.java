package settings;

import com.google.gson.annotations.SerializedName;
import database.DatabaseConfigType;

public class SettingsObject {
    @SerializedName("circles_database")
    public DatabaseConfigType circlesDatabase;

    @SerializedName("bot_database")
    public DatabaseConfigType botDatabase;

    @SerializedName("admin_database")
    public DatabaseConfigType adminDatabase;
}
