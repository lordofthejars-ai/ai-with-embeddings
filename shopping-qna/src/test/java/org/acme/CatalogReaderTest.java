package org.acme;

import org.acme.importer.CatalogReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class CatalogReaderTest {



    @Test
    public void shouldReadRandomData() throws IOException {
        CatalogReader catalogReader = new CatalogReader();
        String path1 = "/Users/asotobue/hadoopdata/train-00000-of-00001-cb803179a865d2bc.parquet";
        List<CatalogReader.Product> products = catalogReader.readRandomProducts(path1);


        Files.write(Paths.get("/tmp/origin.png"), products.getFirst().image(), StandardOpenOption.CREATE);
    }


}
