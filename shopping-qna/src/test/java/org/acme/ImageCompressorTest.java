package org.acme;


import org.acme.utils.ImageCompressor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ImageCompressorTest {


    @Test
    public void shouldResizeImages() throws Exception {

        byte[] image = Files.readAllBytes(Paths.get("/tmp/origin.png"));
        byte[] compressed = ImageCompressor.condense(image, 450, 0.5f);

        Files.write(Paths.get("/tmp/compressed.jpg"), compressed, StandardOpenOption.CREATE);

    }

}