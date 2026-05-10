package org.acme.embeddings;

public record Incident(
        String id,
        String operatingSystem,
        String infrastructure,
        String technology,
        String type,
        String severity,
        boolean networkIssue,
        boolean authIssue,
        boolean memoryIssue,
        boolean apiIssue
) {
}
