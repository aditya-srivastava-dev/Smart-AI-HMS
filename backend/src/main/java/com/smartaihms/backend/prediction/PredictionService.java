package com.smartaihms.backend.prediction;

import com.smartaihms.backend.auth.CurrentUser;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
public class PredictionService {

    public static final String DISCLAIMER =
            "This is only a computer suggestion based on the symptoms you selected. "
          + "It is NOT a medical diagnosis. Please consult a doctor.";

    /** Diseases for which we show an extra "see a doctor soon" warning. */
    private static final Set<String> URGENT = Set.of(
            "Heart attack", "Paralysis (brain hemorrhage)", "Hypoglycemia",
            "Dengue", "Malaria", "Pneumonia", "Typhoid");

    private static final int TOP_N = 3;
    private static final String DATA_FILE = "data/disease_symptoms.csv";

    private final PredictionRepository predictionRepository;
    private final CurrentUser currentUser;

    private DiseaseDataset dataset;
    private NaiveBayesClassifier classifier;
    private Map<String, Object> cachedMetrics;

    public PredictionService(PredictionRepository predictionRepository, CurrentUser currentUser) {
        this.predictionRepository = predictionRepository;
        this.currentUser = currentUser;
    }

    @PostConstruct
    void init() {
        dataset = DiseaseDataset.load(DATA_FILE);
        classifier = new NaiveBayesClassifier();
        classifier.train(dataset.samples(), dataset.symptoms().size());
    }

    public List<String> symptoms() {
        return dataset.symptoms();
    }

    /** Core prediction (no login needed) - also used by tests. */
    public PredictionResponse predict(List<String> selected) {
        if (selected == null || selected.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select at least one symptom");
        }
        boolean[] x = new boolean[dataset.symptoms().size()];
        for (String name : selected) {
            int index = dataset.symptoms().indexOf(name);
            if (index < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown symptom: " + name);
            }
            x[index] = true;
        }

        List<PredictedDisease> top = classifier.predict(x).stream()
                .limit(TOP_N)
                .map(s -> new PredictedDisease(
                        s.label(),
                        Math.round(s.probability() * 1000.0) / 10.0,
                        URGENT.contains(s.label())))
                .toList();
        boolean urgent = top.stream().anyMatch(PredictedDisease::urgent);
        return new PredictionResponse(top, urgent, DISCLAIMER);
    }

    /** Predict for the logged-in user and save it in the history when the user is a patient. */
    public PredictionResponse predictAndSave(List<String> selected) {
        PredictionResponse response = predict(selected);
        if (currentUser.isPatient()) {
            Prediction p = new Prediction();
            p.setPatientId(currentUser.patient().getId());
            p.setSymptoms(new ArrayList<>(selected));
            p.setResults(response.results());
            p.setCreatedAt(Instant.now().toString());
            predictionRepository.save(p);
        }
        return response;
    }

    public List<Prediction> myHistory() {
        currentUser.requireRole("PATIENT");
        return predictionRepository.findByPatientIdOrderByCreatedAtDesc(currentUser.patient().getId());
    }

    public List<Prediction> historyOf(String patientId) {
        currentUser.requirePatientAccess(patientId);
        return predictionRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    /** Accuracy of the model (average of 5 random 80/20 splits, see ModelEvaluator). */
    public synchronized Map<String, Object> metrics() {
        if (cachedMetrics == null) {
            cachedMetrics = ModelEvaluator.evaluate(dataset);
        }
        return cachedMetrics;
    }
}
