package org.acme.importer;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@Singleton
public class Importer {

    @Inject
    CatalogReader catalogReader;

    @Inject
    ItemSampler itemSampler;

    @Inject
    ItemMaterializer itemMaterializer;

    // https://huggingface.co/datasets/KrushiJethe/fashion_data
    @ConfigProperty(name = "parquet.file.location")
    String parquetFile;

    public void importDatasetFromParquetFile() {
        List<CatalogReader.Product> products = catalogReader.readRandomProducts(parquetFile);
        products.stream()
                .map(itemSampler::sampleItemProcess)
                .peek(c -> System.out.println("New Product"))
                .forEach(itemMaterializer::saveItem);
    }

}
