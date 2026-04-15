package com.example.indegenousmedicine2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Central access point for all Firebase services.
 *
 * Usage:
 *   FirebaseManager fm = FirebaseManager.getInstance();
 *   fm.getAuth().getCurrentUser();
 *   fm.getDrugRef().addListenerForSingleValueEvent(…);
 *
 * Centralising here means:
 * - One place to update if the Firebase project changes.
 * - Easier to swap with a test double in unit tests.
 * - Consistent database path constants across the app.
 */
public class FirebaseManager {

    // Database node names — update in one place if the schema changes
    public static final String NODE_DRUGS          = "drug_to_be_validated";
    public static final String NODE_DRUGS_RAW      = "drugs";
    public static final String NODE_PEOPLES        = "Peoples";
    public static final String NODE_IMAGES         = "images";

    private static FirebaseManager instance;

    private final FirebaseAuth auth;
    private final DatabaseReference rootRef;
    private final StorageReference storageRef;

    private FirebaseManager() {
        auth       = FirebaseAuth.getInstance();
        rootRef    = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    // ── Auth ──────────────────────────────────────────────────────────────

    public FirebaseAuth getAuth() {
        return auth;
    }

    /** Returns the signed-in user, or null if not authenticated. */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /** Returns the display name of the signed-in user, or empty string. */
    public String getCurrentUserName() {
        FirebaseUser user = auth.getCurrentUser();
        return (user != null && user.getDisplayName() != null)
                ? user.getDisplayName()
                : "";
    }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    // ── Database ──────────────────────────────────────────────────────────

    public DatabaseReference getRootRef() {
        return rootRef;
    }

    public DatabaseReference getDrugRef() {
        return rootRef.child(NODE_DRUGS);
    }

    public DatabaseReference getDrugsRawRef() {
        return rootRef.child(NODE_DRUGS_RAW);
    }

    public DatabaseReference getPeoplesRef() {
        return rootRef.child(NODE_PEOPLES);
    }

    // ── Storage ───────────────────────────────────────────────────────────

    public StorageReference getStorageRef() {
        return storageRef;
    }

    public StorageReference getImageStorageRef(String fileName) {
        return storageRef.child(NODE_IMAGES).child(fileName);
    }
}
