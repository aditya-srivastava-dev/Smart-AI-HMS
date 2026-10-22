package com.smartaihms.backend.prediction;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PredictionRepository extends MongoRepository<Prediction, String> {

    List<Prediction> findByPatientIdOrderByCreatedAtDesc(String patientId);
}
