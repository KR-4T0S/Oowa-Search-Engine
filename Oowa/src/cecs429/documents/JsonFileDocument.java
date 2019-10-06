package cecs429.documents;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a document that is saved as a simple text file in the local file
 * system.
 */
public class JsonFileDocument implements FileDocument {

    private int mDocumentId;
    private Path mFilePath;
    private String mTitle;

    /**
     * Constructs a TextFileDocument with the given document ID representing the
     * file at the given absolute file path.
     */
    public JsonFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;

        Gson gson = new Gson();
        try (Reader reader = new FileReader(mFilePath.toString())) {
            // Convert JSON File to Java Object
            JsonDoc file = gson.fromJson(reader, JsonDoc.class);
            mTitle = file.getTitle();
            file = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path getFilePath() {
        return mFilePath;
    }

    @Override
    public int getId() {
        return mDocumentId;
    }

    @Override
    public Reader getContent() {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(mFilePath.toString())) {
            // Convert JSON File to Java Object
            JsonDoc file = gson.fromJson(reader, JsonDoc.class);
            Reader result = new StringReader(file.getBody());
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
        return new JsonFileDocument(documentId, absolutePath);
    }
}
