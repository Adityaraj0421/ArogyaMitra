package com.example.indegenousmedicine2;

import android.content.Intent;
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
    private final ExecutorService predictionExecutor = Executors.newSingleThreadExecutor();

    private ImageView previewImageView;
    private TextView predictionTextView;
    private TextView confidenceTextView;
    private TextView statusTextView;
    private ProgressBar progressBar;
    private Button searchDrugButton;
    private String lastPredictedLabel;
    private LeafClassifier leafClassifier;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                try {
                    Bitmap bitmap = loadBitmap(uri);
                    runPrediction(bitmap);
                } catch (IOException e) {
                    showPredictionError();
                }
            });

    private final ActivityResultLauncher<Void> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    runPrediction(bitmap);
                }
            });

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

        previewImageView = findViewById(R.id.previewImageView);
        predictionTextView = findViewById(R.id.predictionValueTextView);
        confidenceTextView = findViewById(R.id.confidenceValueTextView);
        statusTextView = findViewById(R.id.statusTextView);
        progressBar = findViewById(R.id.progressBar);
        Button captureButton = findViewById(R.id.captureButton);
        Button selectButton = findViewById(R.id.selectButton);

        searchDrugButton = findViewById(R.id.searchDrugButton);

        captureButton.setOnClickListener(v -> captureImageLauncher.launch(null));
        selectButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        searchDrugButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DrugListActivity.class);
            intent.putExtra(DrugListActivity.EXTRA_PLANT_QUERY, lastPredictedLabel);
            startActivity(intent);
        });

        try {
            leafClassifier = new LeafClassifier(this);
        } catch (IOException e) {
            captureButton.setEnabled(false);
            selectButton.setEnabled(false);
            statusTextView.setText(R.string.model_load_error);
            Toast.makeText(this, R.string.model_load_error, Toast.LENGTH_LONG).show();
        }
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

    private Bitmap loadBitmap(@NonNull Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
            return ImageDecoder.decodeBitmap(source, (decoder, info, src) -> decoder.setMutableRequired(true));
        }
        return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
    }

    private void runPrediction(@NonNull Bitmap bitmap) {
        if (leafClassifier == null) {
            showPredictionError();
            return;
        }

        previewImageView.setImageBitmap(bitmap);
        progressBar.setVisibility(View.VISIBLE);
        statusTextView.setText(R.string.prediction_loading);

        predictionExecutor.execute(() -> {
            try {
                PredictionResult result = leafClassifier.predict(bitmap);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    statusTextView.setText("");
                    predictionTextView.setText(result.getLabel());
                    confidenceTextView.setText(String.format(Locale.US, "%.2f%%", result.getConfidence() * 100f));
                    lastPredictedLabel = result.getLabel();
                    searchDrugButton.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                runOnUiThread(this::showPredictionError);
            }
        });
    }

    private void showPredictionError() {
        progressBar.setVisibility(View.GONE);
        statusTextView.setText(R.string.prediction_error);
        Toast.makeText(this, R.string.prediction_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        predictionExecutor.shutdownNow();
        if (leafClassifier != null) {
            leafClassifier.close();
        }
    }
}
