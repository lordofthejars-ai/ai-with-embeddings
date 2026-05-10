package org.acme.embeddings;

public class CosineSimilarity {
    private CosineSimilarity() {}

    /**
     * A small value to avoid division by zero.
     */
    public static final float EPSILON = 1e-8f;

    public static double between(double[] vectorA, double[] vectorB) {

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        // Avoid division by zero.
        return dotProduct / Math.max(Math.sqrt(normA) * Math.sqrt(normB), EPSILON);
    }
}
