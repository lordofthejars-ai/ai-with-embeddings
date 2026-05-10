package org.acme;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReciprocalRankFusion {

    public record RankedResult<T>(T item, int rank) {
    }

    public record FusedResult<T>(T item, double score) {
    }

    public <T> List<FusedResult<T>> fuse(
            List<List<RankedResult<T>>> rankedLists,
            int k
    ) {

        Map<T, Double> scores = new HashMap<>();

        for (List<RankedResult<T>> list : rankedLists) {
            for (int i = 0; i < list.size(); i++) {
                RankedResult<T> result = list.get(i);
                double rrfScore = 1.0 / (k + result.rank());
                scores.merge(result.item(), rrfScore, Double::sum);
            }
        }

        return scores.entrySet().stream()
                .map(e -> new FusedResult<>(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingDouble(FusedResult<T>::score).reversed())
                .toList();
    }

}
