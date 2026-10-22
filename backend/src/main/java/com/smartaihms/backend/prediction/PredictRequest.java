package com.smartaihms.backend.prediction;

import java.util.List;

public record PredictRequest(List<String> symptoms) {
}
