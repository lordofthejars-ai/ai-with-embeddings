package org.acme.importer;


import dev.langchain4j.data.image.Image;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.acme.Item;
import org.acme.rag.ImageDescriber;
import org.acme.rag.ImageDescription;

import java.util.Base64;

@Singleton
public class ItemSampler {

    @Inject
    ImageDescriber imageDescriber;

    public Item sampleItemProcess(CatalogReader.Product product) {

        String type = product.articleType();
        String name = product.productName();
        byte[] image = product.image();

        Image b64Image = Image.builder()
                .base64Data(
                    Base64.getEncoder().encodeToString(image)
                    )
                .mimeType("image/jpeg")
                .build();

        //byte[] resizedImage = ImageCompressor.condense(image, 450, 0.5f);

        ImageDescription imageDescription = imageDescriber.processImage(b64Image, type, name);

        return new Item(type, name, imageDescription.summary, imageDescription.imageSummary, imageDescription.tags, b64Image.base64Data());
    }

}
