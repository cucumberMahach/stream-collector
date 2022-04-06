package database;

public enum DatabaseConfigType {
    Local("hibernate.cfg.xml"),
    Remote("hibernate.remote.cfg.xml"),
    LocalOnServer("hibernate.local.german.cfg.xml");

    private final String fileName;

    DatabaseConfigType(String fileName){
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
