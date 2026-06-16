package com.example.indegenousmedicine2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Exercises the OFFLINE TFLite path (LeafClassifier) end-to-end on the device/emulator:
 * loads the bundled model from assets and runs a real inference. This deterministically
 * surfaces any native-library / ABI / op-compat failure that the in-app generic
 * "prediction error" toast hides — without needing the camera, gallery, or Google Sign-In.
 */
@RunWith(AndroidJUnit4.class)
public class LeafClassifierInstrumentedTest {

    private static final String TAG = "LeafClassifierTest";

    @Test
    public void modelLoadsAndRunsInference() throws Exception {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Construction loads the 19 MB model from assets + the TFLite native lib for
        // this device's ABI. If the ABI's .so was stripped (abiFilters), this throws.
        LeafClassifier classifier = new LeafClassifier(ctx);

        // Non-square bitmap to also exercise the v3 centre-crop path.
        Bitmap bmp = Bitmap.createBitmap(240, 320, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.rgb(40, 120, 40));
        Paint p = new Paint();
        p.setColor(Color.rgb(180, 220, 120));
        c.drawCircle(120, 160, 80, p);

        PredictionResult result = classifier.identify(bmp);
        classifier.close();

        Log.i(TAG, "Offline prediction -> label='" + result.getLabel()
                + "' confidence=" + result.getConfidence());

        assertNotNull("Offline classifier returned null result", result);
        assertNotNull("Offline classifier returned null label", result.getLabel());
        assertTrue("Confidence out of [0,1]: " + result.getConfidence(),
                result.getConfidence() >= 0f && result.getConfidence() <= 1f);
    }
}
