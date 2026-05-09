package org.acme;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import smile.classification.KNN;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;

@Path("/email")
public class EmailCreatorResource {

    @Inject
    BgeSmallEnV15EmbeddingModel embeddingModel;

    @Inject
    MilvusEmbeddingStore embeddingStore;

    @Inject
    Logger logger;

    @Channel("email-out")
    Emitter<String> emailEmitter;

    @Inject
    private KNN<double[]> knn;

    @Startup
    public void classifyDataSet() throws IOException {
        //ingestKnnMemory();
        //ingestToDB();
    }

    private void ingestToDB() throws IOException {
        logger.info("Start Ingesting Emails DB");

        Instant initial = Instant.now();

        List<Document> documents = EmailDatasetParser.load("./src/main/resources/spam_ham_dataset.csv");

        logger.info("Reading Mails");

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .documentSplitter(recursive(100, 25))
            .build();

        logger.info("Storing Embeddings");

        ingestor.ingest(documents);

        logger.infof("Finished Ingesting DB in %s seconds", Duration.between(initial, Instant.now()).getSeconds());
    }

    @GET
    @Path("/spam")
    public Response hello2() {
        String newText = "Win a free iPhone now!";
        emailEmitter.send(newText);
        /**float[] newVector = embeddingModel.embed(newText).content().vector();
        double[] vectorDouble = IntStream.range(0, newVector.length)
            .mapToDouble(i -> newVector[i])
            .toArray();
        int predicted = knn.predict(vectorDouble);
        System.out.println(predicted);**/
        return Response.accepted().build();

    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        int k = 7;

        EmbeddingStoreContentRetriever contentRetriever =
            EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(k)
                .build();

        String email = """
            Hello, my name is Alex, I'd like to ask for some information about trains
            """;

        boolean spam = isSpam(contentRetriever, email, k);
        System.out.println(spam);

        email = """
            Buy this product, increase your time. Sales time.
            """;

        spam = isSpam(contentRetriever, email, k);
        System.out.println(spam);
        return "Hello from Quarkus REST";
    }

    private static boolean isSpam(EmbeddingStoreContentRetriever contentRetriever, String email, int k) {
        List<Content> similarEmails = contentRetriever.retrieve(Query.from(email));

        int spamVotes = 0;

        for (Content r : similarEmails) {
            Metadata metadata = r.textSegment().metadata();
            if ("spam".equals(metadata.getString("label"))) {
                spamVotes++;
            }
        }

        boolean spam = (spamVotes > k / 2);
        return spam;
    }

}
