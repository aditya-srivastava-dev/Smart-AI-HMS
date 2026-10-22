package com.smartaihms.backend.prediction;

import java.util.List;

public record PredictionResponse(List<PredictedDisease> results, boolean urgent, String disclaimer) {
}
