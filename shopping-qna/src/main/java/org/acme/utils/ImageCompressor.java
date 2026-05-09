package org.acme.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageCompressor {

    public static byte[] condense(byte[] image, int maxSize, float quality) throws Exception {
        // Load
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(image));

        // Check if image is already smaller than requested size
        int width = original.getWidth();
        int height = original.getHeight();
        int maxDimension = Math.max(width, height);

        if (maxDimension <= maxSize) {
            // Image is already smaller than requested size, return original as base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(original, "jpg", baos);
            return baos.toByteArray();
        }

        // Compute scale
        double scale = (double) maxSize / maxDimension;
        int newW = (int) (width * scale);
        int newH = (int) (height * scale);

        // Resize
        BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newW, newH, null);
        g.dispose();

        // Compress to JPEG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(quality);

        jpgWriter.setOutput(new MemoryCacheImageOutputStream(baos));
        jpgWriter.write(null, new IIOImage(resized, null, null), jpgWriteParam);
        jpgWriter.dispose();

        return baos.toByteArray();
    }
}
