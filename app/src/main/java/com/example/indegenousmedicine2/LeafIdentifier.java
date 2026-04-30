package com.example.indegenousmedicine2;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.io.Closeable;
import java.io.IOException;

public interface LeafIdentifier extends Closeable {

    PredictionResult identify(@NonNull Bitmap bitmap) throws IOException;

    /** Default no-op so implementations with no resources to free don't need to override. */
    @Override
    default void close() {}
}
