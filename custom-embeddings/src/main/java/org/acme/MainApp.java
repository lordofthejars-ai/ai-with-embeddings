package org.acme;


import org.acme.embeddings.CosineSimilarity;
import org.acme.embeddings.Incident;
import org.acme.embeddings.IncidentEmbeddingModel;

import java.util.List;

public class MainApp {

    public static void main(String[] args) {

        Incident incident1 = new Incident(
                "1",
                "Linux",
                "Kubernetes",
                "Quarkus",
                "memory_leak",
                "high",
                false,
                false,
                true,
                true
        );

        Incident incident2 = new Incident(
                "2",
                "Windows",
                "VM",
                "Database",
                "timeout",
                "medium",
                true,
                false,
                false,
                false
        );

        IncidentEmbeddingModel embedding = new IncidentEmbeddingModel();

        double[] embed1 = embedding.embed(incident1);
        double[] embed2 = embedding.embed(incident2);

        Incident query = new Incident(
                "3",
                "Linux",
                "Kubernetes",
                "Java",
                "memory_leak",
                "high",
                false,
                false,
                true,
                true
        );

        double[] embedQ = embedding.embed(query);

        System.out.println(CosineSimilarity.between(embed1, embedQ));
        System.out.println(CosineSimilarity.between(embed2, embedQ));

    }
}
