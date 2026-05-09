package org.acme;

import java.io.IOException;
import java.util.Arrays;

import ai.djl.ModelException;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.translate.TranslateException;

public class ImageMainApp {
    public static void main(String[] args) throws ModelException, IOException, TranslateException {
        String text = "A photo of cats";
        String text2 = "A photo of dogs";
        double[] probs = compareTextAndImage(text, text2);
        System.out.println("%s Probability: %s".formatted(text, probs[0]));
        System.out.println("%s Probability: %s".formatted(text2, probs[1]));
    }

    static double[] compareTextAndImage(String text, String text2)
            throws ModelException, IOException, TranslateException {
        try (ClipModel model = new ClipModel()) {
            String url = "https://resources.djl.ai/images/000000039769.jpg";
            Image img = ImageFactory.getInstance().fromUrl(url);
            float[] logit0 = model.compareTextAndImage(img, text);
            float[] logit1 = model.compareTextAndImage(img, text2);
            double total = Arrays.stream(new double[] {logit0[0], logit1[0]}).map(Math::exp).sum();
            return new double[] {Math.exp(logit0[0]) / total, Math.exp(logit1[0]) / total};
        }
    }
}
