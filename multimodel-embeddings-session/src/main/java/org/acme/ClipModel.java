package org.acme;

import ai.djl.Device;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.NoopTranslator;
import ai.djl.translate.TranslateException;
import ai.djl.util.Pair;

import java.io.IOException;

/**
 * An example of inference using an CLIP model.
 *
 * <p>See this <a
 * href="https://github.com/deepjavalibrary/djl/blob/master/examples/docs/clip_image_text.md">doc</a>
 * for information about this example.
 */
public class ClipModel implements AutoCloseable {

    private ZooModel<NDList, NDList> clip;

    private Predictor<Pair<Image, String>, float[]> imgTextComparator;

    public ClipModel() throws ModelException, IOException {
        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optModelUrls("https://resources.djl.ai/demo/pytorch/clip.zip")
                        .optTranslator(new NoopTranslator())
                        .optEngine("PyTorch")
                        .optDevice(Device.cpu()) // torchscript model only support CPU
                        .build();
        clip = criteria.loadModel();
        imgTextComparator = clip.newPredictor(new ImageTextTranslator());
    }

    public float[] compareTextAndImage(Image image, String text) throws TranslateException {
        return imgTextComparator.predict(new Pair<>(image, text));
    }

    /** {@inheritDoc} */
    @Override
    public void close() {

        imgTextComparator.close();
        clip.close();
    }
}
