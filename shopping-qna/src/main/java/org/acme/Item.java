package org.acme;


import java.util.List;

public record Item(String type, String name, String summary,
                   String imageSummary, List<String> tags, String image) {
}
