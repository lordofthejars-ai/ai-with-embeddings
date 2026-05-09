package org.acme.rag;


import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.acme.Item;
import org.jboss.logging.Logger;

import java.util.List;

@Singleton
public class TextIngestor {

    @Inject
    EmbeddingModel embeddingModel;

    @Named("product_text_db")
    EmbeddingStore<TextSegment> store;

    @Inject
    Logger logger;

    public void ingest(List<Item> items) {

        for (Item item : items) {

            logger.infof("Text Processing of item: %s", item.name());

            String imageSummary = item.imageSummary() == null ? item.summary() : item.imageSummary();
            String summary = item.summary();

            Embedding imageSummaryEmbedding = embeddingModel.embed(imageSummary).content();
            Embedding summaryEmbedding = embeddingModel.embed(summary).content();

            store.add(
                    imageSummaryEmbedding,
                    TextSegment.from(imageSummary, Metadata.metadata("id", item.name()))
            );

            store.add(
                    summaryEmbedding,
                    TextSegment.textSegment(summary, Metadata.metadata("id", item.name()))
            );
        }
    }
}
