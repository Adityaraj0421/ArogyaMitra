package com.example.indegenousmedicine2;

public class PredictionResult {
    private final String label;
    private final float confidence;

    public PredictionResult(String label, float confidence) {
        this.label = label;
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public float getConfidence() {
        return confidence;
    }
}
