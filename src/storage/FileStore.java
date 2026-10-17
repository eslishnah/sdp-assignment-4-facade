package storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStore {
    private final Path folder;

    public FileStore(Path folder) {
        this.folder = folder;
    }

    public void save(String section, String id, String text) {
        validateId(id);
        try {
            Path directory = folder.resolve(section);
            Files.createDirectories(directory);
            Files.writeString(directory.resolve(id + ".csv"), text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save data: " + e.getMessage(), e);
        }
    }

    public String read(String section, String id) {
        validateId(id);
        try {
            Path file = folder.resolve(section).resolve(id + ".csv");
            if (!Files.exists(file)) {
                return null;
            }
            return Files.readString(file, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read data: " + e.getMessage(), e);
        }
    }

    private void validateId(String id) {
        if (id == null || !id.matches("[A-Za-z0-9-]{1,20}")) {
            throw new IllegalArgumentException("Invalid admission ID.");
        }
    }
}
