package com.smartaihms.backend.prediction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A very small Naive Bayes classifier written in plain Java.
 *
 * Idea: for every disease we count how often each symptom appears in the training data.
 * For a new patient we multiply (add the logarithms of):
 *    P(disease)  x  P(symptom 1 | disease)  x  P(symptom 2 | disease) ...
 * for the symptoms the patient has. The disease with the highest score wins.
 * "Laplace smoothing" (+1) makes sure a symptom never seen for a disease does not give zero.
 */
public class NaiveBayesClassifier {

    /** One training row: which symptoms are present (x) and the disease (label). */
    public record Sample(boolean[] x, String label) {
    }

    /** One prediction result: disease and its probability (0 to 1). */
    public record Score(String label, double probability) {
    }

    private static final double ALPHA = 1.0;

    private List<String> labels = new ArrayList<>();
    private double[] logPrior;
    private double[][] logLikelihood; // [disease][symptom] = log P(symptom present | disease)

    public void train(List<Sample> samples, int symptomCount) {
        Map<String, Integer> classIndex = new LinkedHashMap<>();
        for (Sample s : samples) {
            classIndex.putIfAbsent(s.label(), classIndex.size());
        }
        labels = new ArrayList<>(classIndex.keySet());
        int classes = labels.size();

        int[] classCount = new int[classes];
        int[][] symptomCountPerClass = new int[classes][symptomCount];
        for (Sample s : samples) {
            int c = classIndex.get(s.label());
            classCount[c]++;
            for (int j = 0; j < symptomCount; j++) {
                if (s.x()[j]) {
                    symptomCountPerClass[c][j]++;
                }
            }
        }

        logPrior = new double[classes];
        logLikelihood = new double[classes][symptomCount];
        for (int c = 0; c < classes; c++) {
            logPrior[c] = Math.log((classCount[c] + ALPHA) / (samples.size() + ALPHA * classes));
            for (int j = 0; j < symptomCount; j++) {
                logLikelihood[c][j] = Math.log((symptomCountPerClass[c][j] + ALPHA) / (classCount[c] + 2 * ALPHA));
            }
        }
    }

    /** All diseases sorted from most to least likely; probabilities add up to 1. */
    public List<Score> predict(boolean[] x) {
        int classes = labels.size();
        double[] score = new double[classes];
        double max = Double.NEGATIVE_INFINITY;
        for (int c = 0; c < classes; c++) {
            double s = logPrior[c];
            for (int j = 0; j < x.length; j++) {
                if (x[j]) {
                    s += logLikelihood[c][j];
                }
            }
            score[c] = s;
            max = Math.max(max, s);
        }

        // turn log-scores into probabilities (softmax)
        double sum = 0;
        double[] p = new double[classes];
        for (int c = 0; c < classes; c++) {
            p[c] = Math.exp(score[c] - max);
            sum += p[c];
        }
        List<Score> result = new ArrayList<>();
        for (int c = 0; c < classes; c++) {
            result.add(new Score(labels.get(c), p[c] / sum));
        }
        result.sort(Comparator.comparingDouble(Score::probability).reversed());
        return result;
    }
}
