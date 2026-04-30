package com.example.indegenousmedicine2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LeafPredictionActivity extends AppCompatActivity {

    private static final String PREFS_NAME    = "leaf_prediction_prefs";
    private static final String PREF_USE_GEMINI = "use_gemini";

    private final ExecutorService predictionExecutor = Executors.newSingleThreadExecutor();

    // ── Views ──────────────────────────────────────────────────────────────────
    private ImageView previewImageView;
    private TextView  predictionTextView;
    private TextView  confidenceTextView;
    private TextView  statusTextView;
    private TextView  modelBadge;
    private ProgressBar progressBar;
    private Button    searchDrugButton;
    private Button    btnGemini;
    private Button    btnOfflineML;

    private String        lastPredictedLabel;
    private LeafIdentifier identifier;
    private boolean        useGemini;

    // ── Image launchers ────────────────────────────────────────────────────────
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                try {
                    runPrediction(loadBitmap(uri));
                } catch (IOException e) {
                    showPredictionError();
                }
            });

    private final ActivityResultLauncher<Void> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) runPrediction(bitmap);
            });

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageManager.applyLanguage(this);
        setContentView(R.layout.activity_leaf_prediction);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.leaf_prediction_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        previewImageView   = findViewById(R.id.previewImageView);
        predictionTextView = findViewById(R.id.predictionValueTextView);
        confidenceTextView = findViewById(R.id.confidenceValueTextView);
        statusTextView     = findViewById(R.id.statusTextView);
        progressBar        = findViewById(R.id.progressBar);
        searchDrugButton   = findViewById(R.id.searchDrugButton);
        btnGemini          = findViewById(R.id.btnGemini);
        btnOfflineML       = findViewById(R.id.btnOfflineML);
        modelBadge         = findViewById(R.id.modelBadge);

        Button captureButton = findViewById(R.id.captureButton);
        Button selectButton  = findViewById(R.id.selectButton);

        captureButton.setOnClickListener(v -> captureImageLauncher.launch(null));
        selectButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        searchDrugButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DrugListActivity.class);
            intent.putExtra(DrugListActivity.EXTRA_PLANT_QUERY, lastPredictedLabel);
            startActivity(intent);
        });

        btnGemini.setOnClickListener(v -> applyModel(true));
        btnOfflineML.setOnClickListener(v -> applyModel(false));

        // Restore last-used model (default: Gemini)
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        applyModel(prefs.getBoolean(PREF_USE_GEMINI, true));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LanguageManager.applyLanguage(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        predictionExecutor.shutdownNow();
        if (identifier != null) identifier.close();
    }

    // ── Model switching ────────────────────────────────────────────────────────

    /**
     * Swaps the active identifier, refreshes the toggle UI, and clears the
     * previous result. Persists the choice via SharedPreferences so it
     * survives app restarts.
     */
    private void applyModel(boolean gemini) {
        // Release resources held by the previous identifier
        if (identifier != null) identifier.close();

        if (gemini) {
            // Prefer OpenRouter (multi-model, free tier); fall back to native Gemini
            if (!BuildConfig.OPENROUTER_API_KEY.isEmpty()) {
                identifier = new OpenRouterLeafIdentifier(BuildConfig.OPENROUTER_API_KEY);
            } else {
                identifier = new GeminiLeafIdentifier(BuildConfig.GEMINI_API_KEY);
            }
            useGemini = true;
        } else {
            try {
                identifier = new LeafClassifier(this);
                useGemini  = false;
            } catch (IOException e) {
                // TFLite model asset missing — fall back gracefully
                Toast.makeText(this, "Offline model unavailable, using AI",
                        Toast.LENGTH_SHORT).show();
                if (!BuildConfig.OPENROUTER_API_KEY.isEmpty()) {
                    identifier = new OpenRouterLeafIdentifier(BuildConfig.OPENROUTER_API_KEY);
                } else {
                    identifier = new GeminiLeafIdentifier(BuildConfig.GEMINI_API_KEY);
                }
                useGemini = true;
            }
        }

        // Persist choice
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_USE_GEMINI, useGemini)
                .apply();

        updateToggleUI();
        resetResult();
    }

    /** Updates the segmented toggle and the result-card badge to match useGemini. */
    private void updateToggleUI() {
        int activeColor   = 0xFFFFFFFF;
        int inactiveColor = getColor(R.color.green_medium);

        if (useGemini) {
            btnGemini.setBackgroundResource(R.drawable.toggle_selected_bg);
            btnGemini.setTextColor(activeColor);
            btnOfflineML.setBackgroundResource(android.R.color.transparent);
            btnOfflineML.setTextColor(inactiveColor);
            if (modelBadge != null) modelBadge.setText("Gemini AI");
        } else {
            btnOfflineML.setBackgroundResource(R.drawable.toggle_selected_bg);
            btnOfflineML.setTextColor(activeColor);
            btnGemini.setBackgroundResource(android.R.color.transparent);
            btnGemini.setTextColor(inactiveColor);
            if (modelBadge != null) modelBadge.setText("Offline ML");
        }
    }

    /** Clears the previous prediction so stale results aren't shown after a model switch. */
    private void resetResult() {
        predictionTextView.setText(R.string.prediction_placeholder);
        confidenceTextView.setText(R.string.confidence_placeholder);
        statusTextView.setText("");
        searchDrugButton.setVisibility(View.GONE);
        lastPredictedLabel = null;
    }

    // ── Prediction ─────────────────────────────────────────────────────────────

    private void runPrediction(@NonNull Bitmap bitmap) {
        previewImageView.setImageBitmap(bitmap);
        progressBar.setVisibility(View.VISIBLE);
        statusTextView.setText(R.string.prediction_loading);
        searchDrugButton.setVisibility(View.GONE);

        predictionExecutor.execute(() -> {
            try {
                PredictionResult result = identifier.identify(bitmap);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusTextView.setText("");
                    predictionTextView.setText(result.getLabel());
                    confidenceTextView.setText(
                            String.format(Locale.US, "%.0f%%", result.getConfidence() * 100f));
                    lastPredictedLabel = result.getLabel();
                    searchDrugButton.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                runOnUiThread(this::showPredictionError);
            }
        });
    }

    private Bitmap loadBitmap(@NonNull Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
            return ImageDecoder.decodeBitmap(source,
                    (decoder, info, src) -> decoder.setMutableRequired(true));
        }
        return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
    }

    private void showPredictionError() {
        progressBar.setVisibility(View.GONE);
        statusTextView.setText(R.string.prediction_error);
        Toast.makeText(this, R.string.prediction_error, Toast.LENGTH_SHORT).show();
    }
}
