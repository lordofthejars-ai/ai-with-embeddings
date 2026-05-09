package org.acme.rag;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class ClipEmbeddingModel implements EmbeddingModel, AutoCloseable {

    private ZooModel<Image, float[]> clip;
    private Predictor<Image, float[]> imageFeatureExtractor;

    public ClipEmbeddingModel() {
        Criteria<Image, float[]> criteria =
                Criteria.builder()
                        .setTypes(Image.class, float[].class)
                        .optModelUrls("https://resources.djl.ai/demo/pytorch/clip.zip")
                        .optTranslator(new ImageTranslator())
                        .optEngine("PyTorch")
                        .optDevice(Device.cpu()) // torchscript model only support CPU
                        .build();
        try {
            clip = criteria.loadModel();
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new RuntimeException(e);
        }
        imageFeatureExtractor = clip.newPredictor(new ImageTranslator());
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        List<Embedding> embeddings = textSegments.stream()
                .map(s -> Base64.getDecoder().decode(s.text()))
                .map(image -> {
                    try {
                        return ImageFactory.getInstance()
                                .fromInputStream(new ByteArrayInputStream(image));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(i -> {
                    try {
                        return imageFeatureExtractor.predict(i);
                    } catch (TranslateException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(Embedding::from)
                .toList();

        return Response.from(embeddings);
    }

    @Override
    public void close() throws IOException {
        imageFeatureExtractor.close();
        clip.close();
    }
}
