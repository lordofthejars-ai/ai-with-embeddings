package org.acme;

import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import org.slf4j.LoggerFactory;
import smile.classification.KNN;

import java.util.stream.IntStream;

@ApplicationScoped
public class EmailSpamScannerStream {

    @Inject
    private KNN<double[]> knn;

    @Inject
    BgeSmallEnV15EmbeddingModel embeddingModel;

    @Channel("spam-out")
    Emitter<String> spamEmitter;

    @Channel("ham-out")
    Emitter<String> hamEmitter;

    @Inject
    Logger logger;

    @Incoming("email-in")
    public void categorizeEmail(String message) {

        logger.infof("Scanning email: %s", message);

        double[] newVector = embeddingModel.embed(message).content().vectorAsList()
                                                                    .stream()
                                                                    .mapToDouble(Float::doubleValue)
                                                                    .toArray();
        int predicted = knn.predict(newVector);

        if (predicted == 0) {
            logger.info("Ham Email detected.");
            this.hamEmitter.send(message);
        } else {
            logger.info("Spam Email detected.");
            this.spamEmitter.send(message);
        }
    }

    @Incoming("spam-in")
    public void spamEmail(String message) {
        System.out.println(">>>>>>>>> DANGEROUS");
        System.out.println(message);
        System.out.println(">>>>>>>>>>>>>>>>>>>");
    }

    @Incoming("ham-in")
    public void hamEmail(String message) {
        System.out.println(">>>>>>>>> GOOD");
        System.out.println(message);
        System.out.println(">>>>>>>>>>>>>>>>>>>");
    }
}
