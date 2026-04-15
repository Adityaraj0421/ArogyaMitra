package com.example.indegenousmedicine2;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.Closeable;
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

public class LeafClassifier implements Closeable {
    private static final String MODEL_FILE = "leaf_classifier.tflite";
    private static final String LABELS_FILE = "labels.json";
    private static final int IMAGE_SIZE = 150;
    private static final int PIXEL_SIZE = 3;

    private final Interpreter interpreter;
    private final List<String> labels;

    public LeafClassifier(@NonNull Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        interpreter = new Interpreter(loadModelFile(context), options);
        labels = loadLabels(context);
    }

    public PredictionResult predict(@NonNull Bitmap bitmap) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * PIXEL_SIZE)
                .order(ByteOrder.nativeOrder());

        int[] pixels = new int[IMAGE_SIZE * IMAGE_SIZE];
        scaledBitmap.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);

        for (int pixel : pixels) {
            inputBuffer.putFloat(((pixel >> 16) & 0xFF) / 255f);
            inputBuffer.putFloat(((pixel >> 8) & 0xFF) / 255f);
            inputBuffer.putFloat((pixel & 0xFF) / 255f);
        }
        inputBuffer.rewind();

        float[][] output = new float[1][labels.size()];
        interpreter.run(inputBuffer, output);

        int bestIndex = 0;
        float bestScore = output[0][0];
        for (int index = 1; index < output[0].length; index++) {
            if (output[0][index] > bestScore) {
                bestScore = output[0][index];
                bestIndex = index;
            }
        }

        return new PredictionResult(labels.get(bestIndex), bestScore);
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        try (AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_FILE);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {
            return fileChannel.map(FileChannel.MapMode.READ_ONLY,
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getDeclaredLength());
        }
    }

    private List<String> loadLabels(Context context) throws IOException {
        try (InputStream inputStream = context.getAssets().open(LABELS_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            JSONArray jsonArray = new JSONArray(builder.toString());
            List<String> loadedLabels = new ArrayList<>(jsonArray.length());
            for (int index = 0; index < jsonArray.length(); index++) {
                loadedLabels.add(jsonArray.getString(index));
            }
            return loadedLabels;
        } catch (Exception e) {
            throw new IOException(String.format(Locale.US, "Failed to parse %s", LABELS_FILE), e);
        }
    }

    @Override
    public void close() {
        interpreter.close();
    }
}
