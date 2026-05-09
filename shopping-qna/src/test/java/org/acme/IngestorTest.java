package org.acme;


import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.io.IOException;


@QuarkusTest
public class IngestorTest {

    @Inject
    Ingestor ingestor;


    @Test
    void shouldIngestItems() throws IOException {
        ingestor.populate();
    }
}
