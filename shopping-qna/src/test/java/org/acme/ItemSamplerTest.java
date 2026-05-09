package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.importer.CatalogReader;
import org.acme.rag.ImageDescriber;
import org.acme.importer.ItemSampler;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
public class ItemSamplerTest {

    @Inject
    ItemSampler itemSampler;

    @Inject
    CatalogReader catalogReader;

    @Inject
    ImageDescriber imageDescriber;

    @Test
    public void shouldSampleAnItem() throws Exception {

        String path1 = "/Users/asotobue/hadoopdata/train-00000-of-00001-cb803179a865d2bc.parquet";
        List<CatalogReader.Product> products = catalogReader.readRandomProducts(path1);

        /**Image image = Image.builder()
                .base64Data(Base64.getEncoder().encodeToString(products.getFirst().image()))
                .mimeType("image/jpeg")
                .build();

        System.out.println(imageDescriber.describeImage(image));**/
        Item item = itemSampler.sampleItemProcess(products.get(0));

        System.out.println(item.name());
        System.out.println(item.summary());
        System.out.println(item.imageSummary());
        System.out.println(item.tags());
    }

}
