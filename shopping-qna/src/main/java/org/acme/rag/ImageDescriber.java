package org.acme.rag;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import org.eclipse.microprofile.faulttolerance.Retry;

import java.time.temporal.ChronoUnit;

@RegisterAiService
@SystemMessage("""
        You are a service that needs to provide description and properties of the given product image.
        Products are clothes. 
        """)
public interface ImageDescriber {

    @UserMessage("""
            Look at the image and properties of this product and describe it
            
            Format the response as a JSON object with three keys: 'summary', 'image_summary' and 'tags'.
            - 'summary': Summary of product form based on appearance in a sentence.
            - 'image_summary': Describe this image of product based on its type, color, material, pattern, and features.
            - 'tags':  An array of strings representing key features or properties that can represent color, pattern, material, type of the product.
            
            It is important to provide an image summary in the image_summary field of what you see because we'll use this for a RAG application.
            
            The product is a type of {type} and name is {name}.
            """)
    @Retry(maxRetries = 5, delay = 1, delayUnit = ChronoUnit.SECONDS)
    ImageDescription processImage(Image image, String type, String name);

}
