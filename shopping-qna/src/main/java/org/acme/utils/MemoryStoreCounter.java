package org.acme.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.util.List;
import java.util.Map;

public class MemoryStoreCounter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static int count(EmbeddingStore<TextSegment> store) {
        String json = ((InMemoryEmbeddingStore) store).serializeToJson();
        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            List entries = (List) map.get("entries");
            return entries.size();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
