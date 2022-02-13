package util;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.FileReader;

public class MavenData {
    private Model model = null;

    public static final MavenData instance = new MavenData();

    private MavenData(){
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            model = reader.read(new FileReader("pom.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Model getModel(){
        if (model == null)
            return new Model();
        return model;
    }
}
