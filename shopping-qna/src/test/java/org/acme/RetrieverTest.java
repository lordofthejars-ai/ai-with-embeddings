package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

@QuarkusTest
public class RetrieverTest {

    @Inject
    Retriever  retriever;

    @Inject
    Ingestor ingestor;

    @Test
    public void shouldRetrieveContent() throws IOException {
        ingestor.populate();
        List<String> items = retriever.retrieve("I need a sport shoes");
        System.out.println(items);
    }

    @Test
    public void shouldRetrieveContentImage() throws IOException {
        ingestor.populate();
        byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources", "image.jpg"));
        String image  = Base64.getEncoder().encodeToString(bytes);
        List<String> retrieve = retriever.retrieve(image);
        System.out.println(retrieve);

    }
}
