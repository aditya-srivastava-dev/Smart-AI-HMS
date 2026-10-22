package com.smartaihms.backend.prediction;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/** One saved prediction (history of a patient). */
@Document(collection = "predictions")
public class Prediction {

    @Id
    private String id;
    private String patientId;
    private List<String> symptoms;
    private List<PredictedDisease> results;
    private String createdAt;

    public Prediction() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public List<String> getSymptoms() { return symptoms; }
    public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }
    public List<PredictedDisease> getResults() { return results; }
    public void setResults(List<PredictedDisease> results) { this.results = results; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
