package database;

public enum ConfigType {
    Local("hibernate.cfg.xml"),
    Remote("hibernate.remote.cfg.xml");

    private final String fileName;

    ConfigType(String fileName){
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
