package org.acme;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.acme.rag.ClipEmbeddingModel;
import org.acme.utils.Base64Validator;
import org.jboss.logging.Logger;

import java.io.IOException;

import java.util.List;

@ApplicationScoped
public class Retriever {

    @Named("product_text_db")
    EmbeddingStore<TextSegment> textStore;

    @Named("product_image_db")
    EmbeddingStore<TextSegment> imageStore;

    @Inject
    EmbeddingModel textEmbeddingModel;

    @Inject
    Logger logger;

    public List<String> retrieve(String query) {
        if (Base64Validator.isBase64(query)) {
            logger.info("Image Search");

            try(ClipEmbeddingModel imageEmbeddingModel = new ClipEmbeddingModel()) {
                Response<Embedding> embedded = imageEmbeddingModel.embed(query);
                EmbeddingSearchRequest embeddingSearchRequest
                        = EmbeddingSearchRequest.builder()
                        .queryEmbedding(embedded.content())
                        .minScore(0.75)
                        .maxResults(3)
                        .build();

                EmbeddingSearchResult<TextSegment> searchResult = imageStore.search(embeddingSearchRequest);

                return searchResult.matches().stream()
                        .map(c -> c.embedded().metadata())
                        .filter(m -> m.containsKey("id"))
                        .map(m -> m.getString("id"))
                        .distinct()
                        .toList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.info("Text Search");

            Response<Embedding> queryVector = textEmbeddingModel.embed(query);
            EmbeddingSearchRequest embeddingSearchRequest
                    = EmbeddingSearchRequest.builder()
                            .queryEmbedding(queryVector.content())
                            .minScore(0.75)
                            .maxResults(6)
                            .build();

            EmbeddingSearchResult<TextSegment> searchResult = textStore.search(embeddingSearchRequest);

            return searchResult.matches().stream()
                    .map(c -> c.embedded().metadata())
                    .filter(m -> m.containsKey("id"))
                    .map(m -> m.getString("id"))
                    .distinct()
                    .toList();
        }

    }

}
