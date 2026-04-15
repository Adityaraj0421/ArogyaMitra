package com.example.indegenousmedicine2;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DrugDetails extends AppCompatActivity {

    private String currentUser;
    private EditText mMessageEditText;
    private EditText mScientificNameEditText;
    private EditText mHowToApplyEditText;
    private EditText mMedicinalPlantsEditText;
    private EditText mModeOfPreparationEditText;
    private EditText mYearsUsedEditText;
    private EditText mLivingAreaSinceEditText;
    private RadioButton yesRadioButton;
    private RadioButton noRadioButton;
    private android.widget.RadioGroup viableRadioGroup;
    private Button mSendButton;
    private DatabaseReference mMessageDatabaseReference;
    private String user;
    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;
    private final ArrayList<Bitmap> selectedImagesList = new ArrayList<>();

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                try {
                    Bitmap bitmap = loadBitmapFromUri(uri);
                    if (bitmap != null) {
                        addImageToList(bitmap);
                    } else {
                        Toast.makeText(this, R.string.prediction_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(this, R.string.prediction_error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to decode selected image", e);
                }
            });

    private final ActivityResultLauncher<Void> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    addImageToList(bitmap);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageManager.applyLanguage(this);
        setContentView(R.layout.activity_user_details);

        Bundle extras = getIntent().getExtras();
        currentUser = extras != null ? extras.getString("NameOfClient", "Anonymous") : "Anonymous";

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(this, LoginLogic.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Agroषधि");
        }

        imageRecyclerView = findViewById(R.id.imageRecyclerView);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(selectedImagesList);
        imageRecyclerView.setAdapter(imageAdapter);

        setupViews(firebaseUser);

        Button imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(v -> showImageSelectionOptions());
    }

    private void setupViews(FirebaseUser currentFirebaseUser) {
        mMessageDatabaseReference = FirebaseDatabase.getInstance().getReference();
        user = currentFirebaseUser.getDisplayName() != null ? currentFirebaseUser.getDisplayName() : "anonymous";

        mMessageEditText = findViewById(R.id.drugNameEditText);
        mScientificNameEditText = findViewById(R.id.ScientificNameEditText);
        mHowToApplyEditText = findViewById(R.id.howToApplyEditText);
        mMedicinalPlantsEditText = findViewById(R.id.medicinalPlantsEditText);
        mModeOfPreparationEditText = findViewById(R.id.modeOfPreparationEditText);
        viableRadioGroup = findViewById(R.id.viableRadioGroup);
        yesRadioButton = findViewById(R.id.yesRadioButton);
        noRadioButton = findViewById(R.id.noRadioButton);
        mSendButton = findViewById(R.id.sendButton);
        mYearsUsedEditText = findViewById(R.id.yearsUsedEditText);
        mLivingAreaSinceEditText = findViewById(R.id.livingAreaSinceEditText);

        TextInputLayout yearsUsedTextInputLayout = findViewById(R.id.yearsUsedTextInputLayout);
        viableRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.yesRadioButton) {
                yearsUsedTextInputLayout.setVisibility(View.VISIBLE);
            } else {
                yearsUsedTextInputLayout.setVisibility(View.GONE);
                mYearsUsedEditText.setText("");
            }
        });

        mSendButton.setEnabled(false);
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mSendButton.setEnabled(charSequence.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton.setOnClickListener(v -> saveDataToFirebase());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LanguageManager.applyLanguage(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.action_change_language) {
            showLanguageSelectionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLanguageSelectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setItems(R.array.languages, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            updateLocale("en");
                        } else if (which == 1) {
                            updateLocale("as");
                        }
                        refreshUI();
                    }
                })
                .show();
    }

    private void updateLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void refreshUI() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void showImageSelectionOptions() {
        CharSequence[] options = {
                getString(R.string.capture_image),
                getString(R.string.select_image),
                "Cancel"
        };
        new AlertDialog.Builder(this)
                .setTitle("Select Option")
                .setItems(options, (dialog, item) -> {
                    if (item == 0) {
                        captureImageLauncher.launch(null);
                    } else if (item == 1) {
                        pickImageLauncher.launch("image/*");
                    } else {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private Bitmap loadBitmapFromUri(Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
            return ImageDecoder.decodeBitmap(source);
        }
        return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
    }

    private void saveDataToFirebase() {
        String message = mMessageEditText.getText().toString().trim();
        String scientificName = mScientificNameEditText.getText().toString().trim();
        String howToApply = mHowToApplyEditText.getText().toString().trim();
        String medicinalPlants = mMedicinalPlantsEditText.getText().toString().trim();
        String modeOfPreparation = mModeOfPreparationEditText.getText().toString().trim();

        boolean isViable;
        if (yesRadioButton.isChecked()) {
            isViable = true;
        } else if (noRadioButton.isChecked()) {
            isViable = false;
        } else {
            Toast.makeText(this, "Please select whether the drug is viable or not", Toast.LENGTH_SHORT).show();
            return;
        }

        int yearsUsedSince;
        int livingAreaSince;
        try {
            yearsUsedSince = parseIntegerOrZero(mYearsUsedEditText.getText().toString().trim());
            livingAreaSince = parseIntegerOrZero(mLivingAreaSinceEditText.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.isEmpty() || scientificName.isEmpty() || howToApply.isEmpty()
                || medicinalPlants.isEmpty() || modeOfPreparation.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String normalizedScientificName = capitalizeFirstLetter(scientificName.toLowerCase());
        AlertDialog progressDialog = buildProgressDialog();
        progressDialog.show();

        mMessageDatabaseReference.child("drugs")
                .orderByChild("Scientific name")
                .equalTo(normalizedScientificName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            progressDialog.dismiss();
                            Toast.makeText(DrugDetails.this, "Drug with scientific name already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        uploadImagesAndAddDrug(
                                message,
                                normalizedScientificName,
                                howToApply,
                                medicinalPlants,
                                modeOfPreparation,
                                isViable,
                                yearsUsedSince,
                                livingAreaSince,
                                progressDialog
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(DrugDetails.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int parseIntegerOrZero(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private void uploadImagesAndAddDrug(
            String message,
            String scientificName,
            String howToApply,
            String medicinalPlants,
            String modeOfPreparation,
            boolean isViable,
            int yearsUsedSince,
            int livingAreaSince,
            AlertDialog progressDialog
    ) {
        ArrayList<String> imageUrls = new ArrayList<>();
        if (selectedImagesList.isEmpty()) {
            addDrugToDatabase(message, scientificName, howToApply, medicinalPlants, modeOfPreparation,
                    isViable, yearsUsedSince, livingAreaSince, imageUrls, progressDialog);
            return;
        }

        AtomicInteger successfulUploads = new AtomicInteger(0);
        for (Bitmap bitmap : selectedImagesList) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference()
                    .child("images")
                    .child(System.currentTimeMillis() + "_" + successfulUploads.get() + ".jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, baos);
            byte[] data = baos.toByteArray();

            storageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                imageUrls.add(uri.toString());
                                if (successfulUploads.incrementAndGet() == selectedImagesList.size()) {
                                    addDrugToDatabase(message, scientificName, howToApply, medicinalPlants,
                                            modeOfPreparation, isViable, yearsUsedSince, livingAreaSince,
                                            imageUrls, progressDialog);
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(DrugDetails.this, "Failed to fetch uploaded image URL", Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(exception -> {
                        progressDialog.dismiss();
                        Toast.makeText(DrugDetails.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void addDrugToDatabase(
            String message,
            String scientificName,
            String howToApply,
            String medicinalPlants,
            String modeOfPreparation,
            boolean isViable,
            int yearsUsedSince,
            int livingAreaSince,
            ArrayList<String> imageUrls,
            AlertDialog progressDialog
    ) {
        String key = mMessageDatabaseReference.child("drug_to_be_validated").push().getKey();
        if (key == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to create a database entry", Toast.LENGTH_SHORT).show();
            return;
        }

        String basePath = "drug_to_be_validated/" + key + "/";
        Map<String, Object> drugData = new HashMap<>();
        drugData.put(basePath + "Aarogya Mitra", user);
        drugData.put(basePath + "Drug Name", message);
        drugData.put(basePath + "scientificName", scientificName);
        drugData.put(basePath + "howToApply", howToApply);
        drugData.put(basePath + "medicinalPlants", medicinalPlants);
        drugData.put(basePath + "modeOfPreparation", modeOfPreparation);
        drugData.put(basePath + "isViable", isViable);
        drugData.put(basePath + "Client", currentUser);
        if (isViable) {
            drugData.put(basePath + "yearsUsedSince", yearsUsedSince);
            drugData.put(basePath + "livingAreaSince", livingAreaSince);
        }
        for (String url : imageUrls) {
            String imageKey = mMessageDatabaseReference.child("drug_to_be_validated")
                    .child(key).child("imageUrls").push().getKey();
            if (imageKey != null) {
                drugData.put(basePath + "imageUrls/" + imageKey, url);
            }
        }

        mMessageDatabaseReference.updateChildren(drugData)
                .addOnSuccessListener(aVoid -> onDrugSaved(progressDialog))
                .addOnFailureListener(e -> handleDatabaseFailure(progressDialog, "Failed to save drug: " + e.getMessage()));
    }

    private void onDrugSaved(AlertDialog progressDialog) {
        progressDialog.dismiss();
        clearFieldsAndRecyclerView();
        Toast.makeText(this, "Drug added successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, UserInfoActivity.class));
    }

    private void handleDatabaseFailure(AlertDialog progressDialog, String message) {
        progressDialog.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void clearFieldsAndRecyclerView() {
        mMessageEditText.setText("");
        mScientificNameEditText.setText("");
        mHowToApplyEditText.setText("");
        mMedicinalPlantsEditText.setText("");
        mModeOfPreparationEditText.setText("");
        mYearsUsedEditText.setText("");
        mLivingAreaSinceEditText.setText("");
        viableRadioGroup.clearCheck();
        selectedImagesList.clear();
        imageRecyclerView.setVisibility(View.INVISIBLE);
        imageAdapter.notifyDataSetChanged();
    }

    private AlertDialog buildProgressDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        int dp16 = Math.round(16 * getResources().getDisplayMetrics().density);
        layout.setPadding(dp16, dp16, dp16, dp16);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        ProgressBar progressBar = new ProgressBar(this);
        layout.addView(progressBar);

        TextView textView = new TextView(this);
        textView.setText("Uploading data, please wait...");
        textView.setPadding(dp16, 0, 0, 0);
        layout.addView(textView);

        return new AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(false)
                .create();
    }

    private void addImageToList(Bitmap imageBitmap) {
        if (selectedImagesList.size() >= 5) {
            Toast.makeText(this, "You can select up to 5 images", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedImagesList.add(imageBitmap);
        imageRecyclerView.setVisibility(View.VISIBLE);
        imageAdapter.notifyDataSetChanged();
    }
}
