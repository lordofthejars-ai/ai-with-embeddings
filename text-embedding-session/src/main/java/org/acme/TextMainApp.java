package org.acme;

import java.util.Arrays;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.CosineSimilarity;

public class TextMainApp {
    public static void main(String[] args) {
        String pathToModel = "./target/model/model.onnx";
        String pathToTokenizer = "./target/model/tokenizer.json";

        PoolingMode poolingMode = PoolingMode.MEAN;

        //EmbeddingModel embeddingModel = new OnnxEmbeddingModel(pathToModel, pathToTokenizer, poolingMode);
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        Embedding catVector = calculateVector(embeddingModel, "cat");
        Embedding kittenVector = calculateVector(embeddingModel, "kitten");
        Embedding carVector = calculateVector(embeddingModel, "car");
        Embedding gatoVector = calculateVector(embeddingModel, "gato");

        Embedding eatApple = calculateVector(embeddingModel, "I eat everyday an Apple");
        Embedding computerApple = calculateVector(embeddingModel, "I play everyday with my Apple computer");

        printSimilarity(catVector, kittenVector);
        printSimilarity(catVector, carVector);
        printSimilarity(eatApple, computerApple);

    }

    public static void printSimilarity(Embedding e1, Embedding e2) {
        double similirity = CosineSimilarity.between(e1, e2);
        double clampedCosSimilarity = Math.max(0.0, Math.min(1.0, similirity));
        int similarityPercentage = (int) (clampedCosSimilarity * 100);
        System.out.println(similarityPercentage);
    }

    public static Embedding calculateVector(EmbeddingModel embeddingModel, String content) {
        Response<Embedding> response = embeddingModel.embed(content);
        return response.content();
    }

    
}
