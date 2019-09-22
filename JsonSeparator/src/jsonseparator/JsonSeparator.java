package jsonseparator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class JsonSeparator {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        // Get path to json file
        final Path currentWorkingPath = Paths.get("").toAbsolutePath();
        String jsonPath = currentWorkingPath.toString() + "\\all-nps-sites.json";
        
        // Init gson file reader
        Gson gson = new Gson();
        BufferedReader reader = new BufferedReader(new FileReader(jsonPath));
        Type type = new TypeToken<List<JsonDoc>>(){}.getType();
        
        // Convert json data to objects
        List<JsonDoc> docs = gson.fromJson(reader, type);
        
        // Iterate through all objects and write into separate file.
        int counter = 0;
        for (JsonDoc doc: docs) {
            try {
                FileWriter writer = new FileWriter(currentWorkingPath.toString() + "\\jsonfiles\\" + counter + ".json");
                gson.toJson(doc, writer);
                writer.flush();
                writer.close();
                System.out.println("Saved " + counter + ".json");
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
}
