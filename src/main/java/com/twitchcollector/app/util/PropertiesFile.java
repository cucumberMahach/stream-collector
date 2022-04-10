package com.twitchcollector.app.util;

import java.io.IOException;
import java.util.Properties;

public class PropertiesFile {
    public static final PropertiesFile instance = new PropertiesFile();

    private String version = "";
    private String description = "";
    private String artifactId = "";

    private PropertiesFile(){
        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
            version = properties.getProperty("version");
            description = properties.getProperty("description");
            artifactId = properties.getProperty("artifactId");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getArtifactId() {
        return artifactId;
    }
}
