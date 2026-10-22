package com.smartaihms.backend.prediction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Reads data/disease_symptoms.csv: one column per symptom (0/1) and a last column "prognosis". */
public class DiseaseDataset {

    private final List<String> symptoms;
    private final List<NaiveBayesClassifier.Sample> samples;

    private DiseaseDataset(List<String> symptoms, List<NaiveBayesClassifier.Sample> samples) {
        this.symptoms = symptoms;
        this.samples = samples;
    }

    public static DiseaseDataset load(String classpathLocation) {
        try (java.io.InputStream in = DiseaseDataset.class.getClassLoader().getResourceAsStream(classpathLocation)) {
            if (in == null) {
                throw new IllegalStateException("Dataset file not found: " + classpathLocation);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String[] header = reader.readLine().split(",");
            int symptomCount = header.length - 1;
            List<String> symptoms = new ArrayList<>();
            for (int i = 0; i < symptomCount; i++) {
                symptoms.add(header[i].trim());
            }

            List<NaiveBayesClassifier.Sample> samples = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = line.split(",", -1);
                boolean[] x = new boolean[symptomCount];
                for (int i = 0; i < symptomCount; i++) {
                    x[i] = "1".equals(cols[i].trim());
                }
                samples.add(new NaiveBayesClassifier.Sample(x, cols[symptomCount].trim()));
            }
            return new DiseaseDataset(symptoms, samples);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the disease dataset: " + classpathLocation, e);
        }
    }

    public List<String> symptoms() { return symptoms; }

    public List<NaiveBayesClassifier.Sample> samples() { return samples; }
}
