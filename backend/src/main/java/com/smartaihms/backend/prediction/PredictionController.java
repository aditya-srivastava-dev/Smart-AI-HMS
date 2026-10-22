package com.smartaihms.backend.prediction;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prediction")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    // list of symptoms the user can choose from
    @GetMapping("/symptoms")
    public List<String> symptoms() {
        return predictionService.symptoms();
    }

    @PostMapping("/predict")
    public PredictionResponse predict(@RequestBody PredictRequest request) {
        return predictionService.predictAndSave(request.symptoms());
    }

    @GetMapping("/history")
    public List<Prediction> myHistory() {
        return predictionService.myHistory();
    }

    @GetMapping("/patient/{patientId}")
    public List<Prediction> historyOf(@PathVariable String patientId) {
        return predictionService.historyOf(patientId);
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return predictionService.metrics();
    }
}
