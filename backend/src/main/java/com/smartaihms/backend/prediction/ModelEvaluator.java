package com.smartaihms.backend.prediction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Checks how good the model is.
 *
 * The Kaggle file repeats many rows, so the dataset in this project keeps only the unique rows.
 * For each test we shuffle the rows, train on 80% and test on the other 20%.
 * We repeat this with 5 different random seeds and report the average, the lowest and the highest.
 */
public class ModelEvaluator {

    private static final long[] SEEDS = {1, 7, 42, 99, 2024};

    /** Accuracy of one random 80/20 split. */
    public record SplitResult(long seed, int trainRecords, int testRecords, double top1, double top3) {
    }

    public static SplitResult evaluateSplit(DiseaseDataset dataset, long seed) {
        List<NaiveBayesClassifier.Sample> all = new ArrayList<>(dataset.samples());
        Collections.shuffle(all, new Random(seed));
        int trainSize = (int) Math.round(all.size() * 0.8);
        List<NaiveBayesClassifier.Sample> train = all.subList(0, trainSize);
        List<NaiveBayesClassifier.Sample> test = all.subList(trainSize, all.size());

        NaiveBayesClassifier model = new NaiveBayesClassifier();
        model.train(train, dataset.symptoms().size());

        int top1 = 0;
        int top3 = 0;
        for (NaiveBayesClassifier.Sample s : test) {
            List<NaiveBayesClassifier.Score> ranked = model.predict(s.x());
            if (ranked.get(0).label().equals(s.label())) {
                top1++;
            }
            for (int i = 0; i < Math.min(3, ranked.size()); i++) {
                if (ranked.get(i).label().equals(s.label())) {
                    top3++;
                    break;
                }
            }
        }
        return new SplitResult(seed, train.size(), test.size(),
                100.0 * top1 / test.size(), 100.0 * top3 / test.size());
    }

    public static Map<String, Object> evaluate(DiseaseDataset dataset) {
        List<SplitResult> results = new ArrayList<>();
        for (long seed : SEEDS) {
            results.add(evaluateSplit(dataset, seed));
        }
        double avg1 = results.stream().mapToDouble(SplitResult::top1).average().orElse(0);
        double avg3 = results.stream().mapToDouble(SplitResult::top3).average().orElse(0);
        double min1 = results.stream().mapToDouble(SplitResult::top1).min().orElse(0);
        double max1 = results.stream().mapToDouble(SplitResult::top1).max().orElse(0);
        SplitResult first = results.get(0);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("method", "Naive Bayes (Laplace smoothing)");
        m.put("diseases", dataset.samples().stream().map(NaiveBayesClassifier.Sample::label).distinct().count());
        m.put("symptoms", dataset.symptoms().size());
        m.put("uniqueRecords", dataset.samples().size());
        m.put("trainRecords", first.trainRecords());
        m.put("testRecords", first.testRecords());
        m.put("splitsTested", results.size());
        m.put("accuracyPercent", round1(avg1));
        m.put("lowestAccuracyPercent", round1(min1));
        m.put("highestAccuracyPercent", round1(max1));
        m.put("top3AccuracyPercent", round1(avg3));
        return m;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
