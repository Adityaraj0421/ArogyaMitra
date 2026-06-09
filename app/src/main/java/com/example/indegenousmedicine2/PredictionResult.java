package com.example.indegenousmedicine2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // ── AI response parsing ─────────────────────────────────────────────────────

    private static final Pattern PLANT_PATTERN = Pattern.compile(
            "PLANT:\\s*(.+?)\\s*(?=CONFIDENCE:|USES:|\\n|$)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern CONFIDENCE_PATTERN = Pattern.compile(
            "CONFIDENCE:\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE);

    /**
     * Robustly parses a vision-model reply into a label + confidence.
     *
     * Handles all the shapes the NVIDIA / Gemini models actually return:
     *   - the requested three-line format,
     *   - all three labels crammed onto one line (no newlines), and
     *   - free-form prose with no labels at all.
     *
     * The old line-by-line parser only worked for the first case, which is why
     * a single-line reply dumped "PLANT: … CONFIDENCE: 85 USES: …" straight into
     * the plant-name field with 0% confidence.
     */
    public static PredictionResult fromAiText(String text) {
        if (text == null) return new PredictionResult("Unknown", 0f);
        text = text.trim();

        float confidence = 0f;
        Matcher cm = CONFIDENCE_PATTERN.matcher(text);
        if (cm.find()) {
            int pct = Integer.parseInt(cm.group(1));
            confidence = Math.max(0, Math.min(100, pct)) / 100f;
        }

        String plant = null;
        Matcher pm = PLANT_PATTERN.matcher(text);
        if (pm.find()) {
            plant = pm.group(1).trim();
        }

        // No "PLANT:" label (prose reply) — fall back to the first non-empty line.
        if (plant == null || plant.isEmpty()) {
            for (String line : text.split("\\n")) {
                line = line.trim();
                if (!line.isEmpty()) { plant = line; break; }
            }
        }
        if (plant == null || plant.isEmpty()) plant = "Unknown";

        // Tidy: strip trailing punctuation, cap absurdly long prose.
        plant = plant.replaceAll("[\\s\\-•:;,.]+$", "").trim();
        if (plant.length() > 80) plant = plant.substring(0, 80).trim() + "…";

        return new PredictionResult(plant, confidence);
    }
}
