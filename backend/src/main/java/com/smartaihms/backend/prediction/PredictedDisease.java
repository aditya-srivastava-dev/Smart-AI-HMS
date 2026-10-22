package com.smartaihms.backend.prediction;

/** One suggested disease. probability is in percent (0-100). */
public record PredictedDisease(String disease, double probability, boolean urgent) {
}
