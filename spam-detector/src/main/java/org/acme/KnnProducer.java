package org.acme;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
import dev.langchain4j.model.output.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import smile.classification.KNN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@ApplicationScoped
public class KnnProducer {

    @Inject
    BgeSmallEnV15EmbeddingModel embeddingModel;

    @Produces
    KNN<double[]> createKnnClusters() throws IOException {
        return ingestKnnMemory();
    }

    private KNN<double[]> ingestKnnMemory() throws IOException {
        List<double[]> features = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        List<Document> documents = EmailDatasetParser.load("./src/main/resources/spam_ham_dataset.csv");

        for (Document document : documents) {
            Response<Embedding> embedded = embeddingModel.embed(document.text());
            float[] vector = embedded.content().vector();
            features.add(IntStream.range(0, vector.length).mapToDouble(i -> vector[i]).toArray());
            labels.add(label(document));
        }

        double[][] X = features.toArray(new double[0][]);
        int[] y = labels.stream().mapToInt(i -> i).toArray();

        int k = 7;
        return KNN.fit(X, y, k);
    }

    private int label(Document document) {
        String label = document.metadata().getString("label");
        return "spam".equals(label) ? 1 : 0;
    }

}
