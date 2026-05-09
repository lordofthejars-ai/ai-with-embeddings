package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.importer.Importer;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ImporterTest {

    @Inject
    Importer importer;

    @Test
    void shouldImport() {
        importer.importDatasetFromParquetFile();
    }

}
