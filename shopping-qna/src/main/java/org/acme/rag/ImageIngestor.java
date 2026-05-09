package org.acme.rag;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.acme.Item;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.List;

@Singleton
public class ImageIngestor {

    @Named("product_image_db")
    EmbeddingStore<TextSegment> store;

    @Inject
    Logger logger;

    public void ingest(List<Item> items) {

        try(ClipEmbeddingModel imageEmbeddings = new ClipEmbeddingModel()) {

            for (Item item : items) {

                logger.infof("Image Processing of item: %s", item.name());

                String image = item.image();
                Response<Embedding> embedded = imageEmbeddings.embed(image);
                TextSegment textSegment = TextSegment.from(image,
                        Metadata.metadata("id", item.name()));

                store.add(embedded.content(), textSegment);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
