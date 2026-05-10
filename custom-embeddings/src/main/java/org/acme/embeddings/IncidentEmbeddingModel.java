package org.acme.embeddings;

import java.util.ArrayList;
import java.util.List;

public class IncidentEmbeddingModel {

    public double[] embed(Incident incident) {
        List<Double> vector = new ArrayList<>();

        addOperatingSystem(vector, incident.operatingSystem());
        addInfrastructure(vector, incident.infrastructure());
        addTechnology(vector, incident.technology());
        addIncidentType(vector, incident.type());
        addSeverity(vector, incident.severity());

        vector.add(incident.networkIssue() ? 1d : 0d);
        vector.add(incident.authIssue() ? 1d : 0d);
        vector.add(incident.memoryIssue() ? 1d : 0d);
        vector.add(incident.apiIssue() ? 1d : 0d);

        return toDoubleArray(vector);
    }

    private double[] toDoubleArray(List<Double> list) {

        double[] arr = new double[list.size()];

        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }

        return arr;
    }

    private void addOperatingSystem(List<Double> v, String os) {

        os = os.toLowerCase();

        if (os.contains("windows server 2016")) {
            add(v, 1d,0d,0d,0d, 1d,0d,0d, 0.1d,1d,0.60d);
        } else if (os.contains("windows server 2019")) {
            add(v, 1f,0f,0f,0f, 1f,0f,0f, 0.1f,1f,0.80f);
        } else if (os.contains("windows server 2022")) {
            add(v, 1f,0f,0f,0f, 1f,0f,0f, 0.1f,1f,1.00f);
        } else if (os.contains("ubuntu 20.04")) {
            add(v, 0f,1f,0.1f,0f, 0f,0f,1f, 0.3f,0.9f,0.75f);
        } else if (os.contains("ubuntu 22.04")) {
            add(v, 0f,1f,0.1f,0f, 0f,0f,1f, 0.3f,0.9f,0.90f);
        } else if (os.contains("rhel 9") || os.contains("red hat 9")) {
            add(v, 0f,1f,0.1f,0f, 0f,1f,0.1f, 0.2f,1f,0.95f);
        } else {
            add(v, 0.3f,0.3f,0f,0f, 0.2f,0.2f,0.2f, 0.5f,0.5f,0.5f);
        }
    }

    private void addInfrastructure(List<Double> v, String infra) {

        switch (infra.toLowerCase()) {
            case "kubernetes" -> {
                v.add(1d); v.add(0d); v.add(0d);
            }
            case "vm" -> {
                v.add(0d); v.add(1d); v.add(0d);
            }
            case "baremetal" -> {
                v.add(0d); v.add(0d); v.add(1d);
            }
            default -> {
                v.add(0.2d); v.add(0.2d); v.add(0.2d);
            }
        }
    }

    private void addTechnology(List<Double> v, String tech) {

        switch (tech.toLowerCase()) {
            case "java" -> {
                v.add(1d); v.add(0d); v.add(0d);
            }
            case "spring" -> {
                v.add(0.9d); v.add(0.1d); v.add(0d);
            }
            case "quarkus" -> {
                v.add(0.8d); v.add(0.1d); v.add(0d);
            }
            case "kubernetes" -> {
                v.add(0d); v.add(1d); v.add(0d);
            }
            case "database" -> {
                v.add(0d); v.add(0d); v.add(1d);
            }
            case "Nginx" -> {
                v.add(0d); v.add(0.7d); v.add(0d);
            }
            case "Ingress" -> {
                v.add(0d); v.add(0.8d); v.add(0d);
            }
            case "Gateway" -> {
                v.add(0d); v.add(0.9d); v.add(0d);
            }
            default -> {
                v.add(0.3d); v.add(0.3d); v.add(0.3d);
            }
        }
    }

    private void addIncidentType(List<Double> v, String type) {

        switch (type.toLowerCase()) {
            case "timeout" -> {
                v.add(1d); v.add(0d); v.add(0d);
            }
            case "memory_leak" -> {
                v.add(0d); v.add(1d); v.add(0d);
            }
            case "crash" -> {
                v.add(0d); v.add(0d); v.add(1d);
            }
            default -> {
                v.add(0.2d); v.add(0.2d); v.add(0.2d);
            }
        }
    }

    private void addSeverity(List<Double> v, String severity) {

        switch (severity.toLowerCase()) {
            case "low" -> v.add(0.2d);
            case "medium" -> v.add(0.5d);
            case "high" -> v.add(1d);
            default -> v.add(0.5d);
        }
    }

    private void add(List<Double> vector, double... values) {
        for (double value : values) {
            vector.add(value);
        }
    }

}
