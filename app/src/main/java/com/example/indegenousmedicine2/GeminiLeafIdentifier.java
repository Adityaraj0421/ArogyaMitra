package com.example.indegenousmedicine2;

import android.graphics.Bitmap;
import android.util.Base64;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiLeafIdentifier implements LeafIdentifier {

    // gemini-2.0-flash was retired ("no longer available", HTTP 404). gemini-2.5-flash
    // is the current stable multimodal model and is verified working with our key + the
    // inline_data request shape below.
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    // ─────────────────────────────────────────────────────────────────────────
    // YOUR TURN: Write the prompt that Gemini will receive along with the image.
    //
    // The prompt must produce output this parser can read — each line starts
    // with a known label. The parser currently reads:
    //
    //   PLANT: <scientific name (common name)>
    //   CONFIDENCE: <0-100>
    //   USES: <one sentence>
    //
    // Design choices to consider:
    //   • Should USES be one sentence or bullet points?
    //   • Should it include Assamese / local names?
    //   • What should it say when the leaf is not a medicinal plant?
    //   • Should it warn if image quality is too low to be sure?
    //
    // Keep the PLANT / CONFIDENCE / USES labels — the parser below depends on them.
    // Add extra labels (e.g. REGION:, DOSAGE:) and extend parseResponse() to read them.
    // ─────────────────────────────────────────────────────────────────────────
    private static final String GEMINI_PROMPT =
            "You are a medicinal plant expert specialising in indigenous plants of South and South-East Asia.\n" +
            "A user has photographed a plant leaf. Identify it.\n\n" +
            "Respond in exactly this format — no extra text before or after:\n" +
            "PLANT: [Scientific Name (Common Name)]\n" +
            "CONFIDENCE: [integer 0-100]\n" +
            "USES: [one sentence describing its primary medicinal use]\n\n" +
            "If you cannot identify the plant, use:\n" +
            "PLANT: Unknown\n" +
            "CONFIDENCE: 0\n" +
            "USES: Unable to identify — please retake the photo with better lighting.";

    private final OkHttpClient client;
    private final String apiKey;

    public GeminiLeafIdentifier(@NonNull String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /** Sends the bitmap to Gemini and returns a PredictionResult. Blocking — call off the main thread. */
    public PredictionResult identify(@NonNull Bitmap bitmap) throws IOException {
        String requestJson = buildRequestJson(bitmap);

        Request request = new Request.Builder()
                .url(ENDPOINT + "?key=" + apiKey)
                .post(RequestBody.create(
                        requestJson,
                        MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Gemini API error: HTTP " + response.code());
            }
            return parseResponse(response.body().string());
        }
    }

    private String buildRequestJson(@NonNull Bitmap bitmap) throws IOException {
        try {
            String base64Image = bitmapToBase64(bitmap);

            JSONObject inlineData = new JSONObject();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", base64Image);

            JSONObject imagePart = new JSONObject();
            imagePart.put("inline_data", inlineData);

            JSONObject textPart = new JSONObject();
            textPart.put("text", GEMINI_PROMPT);

            JSONArray parts = new JSONArray();
            parts.put(imagePart);
            parts.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONObject body = new JSONObject();
            body.put("contents", new JSONArray().put(content));

            return body.toString();
        } catch (Exception e) {
            throw new IOException("Failed to build Gemini request", e);
        }
    }

    private PredictionResult parseResponse(@NonNull String jsonString) throws IOException {
        try {
            JSONObject json = new JSONObject(jsonString);
            String text = json
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            // Robustly handles one-line, multi-line, or prose replies.
            return PredictionResult.fromAiText(text);
        } catch (Exception e) {
            throw new IOException("Failed to parse Gemini response: " + jsonString, e);
        }
    }

    private String bitmapToBase64(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }
}
