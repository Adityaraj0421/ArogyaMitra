package com.example.indegenousmedicine2;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Offline leaf classifier backed by a TFLite MobileNet model.
 * Implements LeafIdentifier so it can be hot-swapped with GeminiLeafIdentifier.
 *
 * Model input : 150×150 RGB float32 normalised to [0, 1]
 * Model output: float[1][numLabels] softmax confidence scores
 */
public class LeafClassifier implements LeafIdentifier {

    private static final String MODEL_FILE  = "leaf_classifier.tflite";
    private static final String LABELS_FILE = "labels.json";
    private static final int    IMAGE_SIZE  = 150;
    private static final int    PIXEL_SIZE  = 3;   // R, G, B channels

    private final Interpreter  interpreter;
    private final List<String> labels;

    public LeafClassifier(@NonNull Context context) throws IOException {
        Interpreter.Options opts = new Interpreter.Options();
        opts.setNumThreads(4);
        interpreter = new Interpreter(loadModelFile(context), opts);
        labels      = loadLabels(context);
    }

    @Override
    public PredictionResult identify(@NonNull Bitmap bitmap) throws IOException {
        // Scale to model's expected input size
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);

        // Pack pixels into a float ByteBuffer, normalising each channel to [0, 1]
        ByteBuffer inputBuffer = ByteBuffer
                .allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * PIXEL_SIZE)
                .order(ByteOrder.nativeOrder());

        int[] pixels = new int[IMAGE_SIZE * IMAGE_SIZE];
        scaled.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);
        for (int pixel : pixels) {
            inputBuffer.putFloat(((pixel >> 16) & 0xFF) / 255f);   // R
            inputBuffer.putFloat(((pixel >> 8)  & 0xFF) / 255f);   // G
            inputBuffer.putFloat(( pixel        & 0xFF) / 255f);   // B
        }
        inputBuffer.rewind();

        // Run inference
        float[][] output = new float[1][labels.size()];
        interpreter.run(inputBuffer, output);

        // Argmax — pick the class with highest confidence
        int   bestIndex = 0;
        float bestScore = output[0][0];
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > bestScore) {
                bestScore = output[0][i];
                bestIndex = i;
            }
        }

        return new PredictionResult(labels.get(bestIndex), bestScore);
    }

    // ── Resource loading ──────────────────────────────────────────────────────

    private MappedByteBuffer loadModelFile(@NonNull Context context) throws IOException {
        try (AssetFileDescriptor fd  = context.getAssets().openFd(MODEL_FILE);
             FileInputStream    fis = new FileInputStream(fd.getFileDescriptor());
             FileChannel        fc  = fis.getChannel()) {
            return fc.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
        }
    }

    private List<String> loadLabels(@NonNull Context context) throws IOException {
        try (InputStream     is = context.getAssets().open(LABELS_FILE);
             BufferedReader  br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            JSONArray arr    = new JSONArray(sb.toString());
            List<String> lst = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) lst.add(arr.getString(i));
            return lst;
        } catch (Exception e) {
            throw new IOException(
                    String.format(Locale.US, "Failed to parse %s", LABELS_FILE), e);
        }
    }

    @Override
    public void close() {
        interpreter.close();
    }
}
