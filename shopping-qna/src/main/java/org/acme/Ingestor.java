package org.acme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.acme.persistence.Product;
import org.acme.rag.ImageIngestor;
import org.acme.rag.TextIngestor;
import org.acme.utils.MemoryStoreCounter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class Ingestor {

    public static final String TEXT_STORE_BACKUP = "0textStore.backup";
    public static final String IMAGE_STORE_BACKUP = "0imageStore.backup";

    @Named("product_text_db")
    EmbeddingStore<TextSegment> textStore;

    @Named("product_image_db")
    EmbeddingStore<TextSegment> imageStore;

    @Inject
    TextIngestor textIngestor;

    @Inject
    ImageIngestor imageIngestor;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Logger logger;

    @ConfigProperty(name = "materialized.items.path", defaultValue = "./src/main/resources/data")
    String dataFolder;

    @ConfigProperty(name = "process.vectors", defaultValue = "false")
    boolean processVectors;

    public void populate() throws IOException {
        // Only runs when database is empty
        if (Product.count() == 0) {
            if (processVectors) {

                this.ingest();
                this.storeToDB();

            } else {
                // Vector Stores are automatically
                // populated with data as the Producer takes care of this logic (EmbeddingStoreProducer)
                this.storeToDB();

                logger.infof("Text Vector Store Loaded Entries: %s", MemoryStoreCounter.count(textStore));
                logger.infof("Image Vector Store Loaded Entries: %s", MemoryStoreCounter.count(imageStore));
            }
        }
    }



    public void storeToDB() throws IOException {

        final List<Item> items = loadItems();
        QuarkusTransaction.begin();

        items.stream()
                .peek(i -> logger.infof("Storing Item: %s", i.name()))
                .map(i -> {
                    Product product = new Product();

                    product.name = i.name();
                    product.image = i.image();
                    product.imageSummary = i.imageSummary();
                    product.summary = i.summary();
                    product.tags = i.tags();
                    product.type = i.type();

                    return product;
                })
                .forEach(p -> p.persist());

        QuarkusTransaction.commit();

    }

    public void ingest() throws IOException {

        final List<Item> items = loadItems();

        imageIngestor.ingest(items);
        textIngestor.ingest(items);

        Path pathDataFolder = Paths.get(dataFolder);
        ((InMemoryEmbeddingStore<TextSegment>) textStore)
                .serializeToFile(pathDataFolder.resolve(TEXT_STORE_BACKUP));

        ((InMemoryEmbeddingStore<TextSegment>) imageStore)
                .serializeToFile(pathDataFolder.resolve(IMAGE_STORE_BACKUP));

    }

    private List<Item> loadItems() throws IOException {
        Path dir = FileSystems.getDefault().getPath(dataFolder);
        List<Item> items = new ArrayList<>();
        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(dir, "*.json")) {


            for (Path itemFile : stream) {
                Item item = objectMapper
                        .readValue(itemFile.toAbsolutePath().toFile(), Item.class);
                items.add(item);
            }
        }
        return items;
    }
}
