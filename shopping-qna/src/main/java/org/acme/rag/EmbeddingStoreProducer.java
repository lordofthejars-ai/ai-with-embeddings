package org.acme.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.acme.Ingestor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class EmbeddingStoreProducer {

    @ConfigProperty(name = "process.vectors", defaultValue = "false")
    boolean processVectors;

    @ConfigProperty(name = "materialized.items.path", defaultValue = "./src/main/resources/data")
    String dataFolder;

    @Produces
    @Named("product_image_db")
    @Singleton
    InMemoryEmbeddingStore<TextSegment> createProductImageVectorStore() {
        if (processVectors) {
            return new InMemoryEmbeddingStore<>();
        } else {
            Path pathDataFolder = Paths.get(dataFolder);
            return InMemoryEmbeddingStore
                    .fromFile(pathDataFolder.resolve(Ingestor.IMAGE_STORE_BACKUP));
        }
    }

    @Produces
    @Named("product_text_db")
    @Singleton
    InMemoryEmbeddingStore<TextSegment> createProductTextVectorStore() {
        if (processVectors) {
            return new InMemoryEmbeddingStore<>();
        } else {
            Path pathDataFolder = Paths.get(dataFolder);
            return InMemoryEmbeddingStore
                    .fromFile(pathDataFolder.resolve(Ingestor.TEXT_STORE_BACKUP));
        }
    }



}
