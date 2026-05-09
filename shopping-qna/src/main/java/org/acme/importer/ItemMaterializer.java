package org.acme.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.acme.Item;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class ItemMaterializer {

    @ConfigProperty(name = "materialized.items.path", defaultValue = "./src/main/resources/data")
    String dataFolder;

    @Inject
    ObjectMapper objectMapper;

    public void saveItem(Item item) {
        Path file = Paths.get(dataFolder).resolve(item.name() + ".json");
        try {
            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(file.toAbsolutePath().toFile(), item);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
