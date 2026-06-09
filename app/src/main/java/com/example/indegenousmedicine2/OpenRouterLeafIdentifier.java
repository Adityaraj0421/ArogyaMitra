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

/**
 * Leaf identifier that routes through OpenRouter's OpenAI-compatible API.
 * Uses the best available free vision model with automatic fallback ordering.
 *
 * Request shape (OpenAI chat-completions format):
 *   POST https://openrouter.ai/api/v1/chat/completions
 *   Authorization: Bearer <key>
 *   { "model": "...", "messages": [{ "role":"user", "content": [ image_url, text ] }] }
 *
 * This is structurally different from the native Gemini format used in
 * GeminiLeafIdentifier (which puts images inside contents[].parts[].inline_data).
 */
public class OpenRouterLeafIdentifier implements LeafIdentifier {

    private static final String ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";

    // Free non-Google vision models — confirmed working with image input.
    // Both are NVIDIA reasoning models: response may be in "reasoning" field when "content" is null.
    private static final String[] MODEL_PRIORITY = {
            "nvidia/nemotron-nano-12b-v2-vl:free",
            "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free"
    };

    private static final String PROMPT =
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

    public OpenRouterLeafIdentifier(@NonNull String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public PredictionResult identify(@NonNull Bitmap bitmap) throws IOException {
        String base64Image = bitmapToBase64(bitmap);
        IOException lastError = null;

        for (String model : MODEL_PRIORITY) {
            try {
                return callModel(model, base64Image);
            } catch (IOException e) {
                lastError = e;
                // try next model in list
            }
        }
        throw lastError != null ? lastError
                : new IOException("All OpenRouter models failed");
    }

    // ── HTTP call ─────────────────────────────────────────────────────────────

    private PredictionResult callModel(@NonNull String model,
                                       @NonNull String base64Image) throws IOException {
        String body = buildRequestJson(model, base64Image);

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("HTTP-Referer", "https://arogyamitra.app")
                .addHeader("X-Title", "ArogyaMitra")
                .post(RequestBody.create(body,
                        MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("OpenRouter error " + response.code()
                        + " (model=" + model + "): " + responseBody);
            }
            return parseResponse(responseBody);
        }
    }

    // ── Request builder ───────────────────────────────────────────────────────

    private String buildRequestJson(@NonNull String model,
                                    @NonNull String base64Image) throws IOException {
        try {
            // Image part — OpenAI vision format
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", "data:image/jpeg;base64," + base64Image);

            JSONObject imagePart = new JSONObject();
            imagePart.put("type", "image_url");
            imagePart.put("image_url", imageUrl);

            // Text part
            JSONObject textPart = new JSONObject();
            textPart.put("type", "text");
            textPart.put("text", PROMPT);

            // Message
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", new JSONArray().put(imagePart).put(textPart));

            // Full body
            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("messages", new JSONArray().put(message));
            body.put("max_tokens", 600);   // reasoning models need extra tokens for chain-of-thought

            return body.toString();
        } catch (Exception e) {
            throw new IOException("Failed to build OpenRouter request", e);
        }
    }

    // ── Response parser ───────────────────────────────────────────────────────

    private PredictionResult parseResponse(@NonNull String jsonString) throws IOException {
        try {
            JSONObject json = new JSONObject(jsonString);

            // OpenAI format: choices[0].message.content
            // NVIDIA reasoning models put output in "reasoning" when "content" is null —
            // check both fields so the same parser works for all model types.
            JSONObject message = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message");

            String text = message.isNull("content")
                    ? message.optString("reasoning", "")
                    : message.getString("content");

            // Robustly handles one-line, multi-line, or prose replies.
            return PredictionResult.fromAiText(text);
        } catch (Exception e) {
            throw new IOException("Failed to parse OpenRouter response: " + jsonString, e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String bitmapToBase64(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }
}
