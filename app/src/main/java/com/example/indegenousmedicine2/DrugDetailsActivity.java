// DrugDetailsActivity.java
package com.example.indegenousmedicine2;

import android.os.Bundle;
import static android.content.ContentValues.TAG;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DrugDetailsActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    private TextView textViewAarogyaMitra, textViewDrugName, textViewHowToApply, textViewIsViable, textViewMedicinalPlants, textViewModeOfPreparation, textViewScientificName, textViewYearsUsedSince;
    private RecyclerView recyclerViewImages;
    private ImageAdapter imageAdapter;
    private List<String> imageUrls = new ArrayList<>();
    private DatabaseReference mMessageDatabaseReference;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_details);

        // Initialize views
        textViewAarogyaMitra = findViewById(R.id.textViewAarogyaMitra);
        textViewDrugName = findViewById(R.id.textViewDrugName);
        textViewHowToApply = findViewById(R.id.textViewHowToApply);
        textViewIsViable = findViewById(R.id.textViewIsViable);
        textViewMedicinalPlants = findViewById(R.id.textViewMedicinalPlants);
        textViewModeOfPreparation = findViewById(R.id.textViewModeOfPreparation);
        textViewScientificName = findViewById(R.id.textViewScientificName);
        textViewYearsUsedSince = findViewById(R.id.textViewYearsUsedSince);
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(imageUrls);
        recyclerViewImages.setAdapter(imageAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Drug Details");

        // Initialize Firebase references
        mMessageDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        // Get drug key from intent
        String drugKey = getIntent().getStringExtra("DRUG_KEY");

        // Fetch drug details
        fetchDrugDetails(drugKey);
    }

    private void fetchDrugDetails(String drugKey) {
        mMessageDatabaseReference.child("drug_to_be_validated").child(drugKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String aarogyaMitra = snapshot.child("Aarogya Mitra").getValue(String.class);
                String drugName = snapshot.child("Drug Name").getValue(String.class);
                String howToApply = snapshot.child("howToApply").getValue(String.class);
                Boolean isViable = snapshot.child("isViable").getValue(Boolean.class);
                String medicinalPlants = snapshot.child("medicinalPlants").getValue(String.class);
                String modeOfPreparation = snapshot.child("modeOfPreparation").getValue(String.class);
                String scientificName = snapshot.child("scientificName").getValue(String.class);
                Long yearsUsedSince = snapshot.child("yearsUsedSince").getValue(Long.class);

                textViewAarogyaMitra.setText(getString(R.string.aarogya_mitra) + (aarogyaMitra != null ? aarogyaMitra : getString(R.string.not_available)));
                textViewDrugName.setText(getString(R.string.name) + (drugName != null ? drugName : getString(R.string.not_available)));
                textViewHowToApply.setText(getString(R.string.how_to_apply) + (howToApply != null ? howToApply : getString(R.string.not_available)));
                textViewIsViable.setText(getString(R.string.is_viable) + (isViable != null ? (isViable ? getString(R.string.yes) : getString(R.string.no)) : getString(R.string.not_available)));
                textViewMedicinalPlants.setText(getString(R.string.medicinal_plants) + (medicinalPlants != null ? medicinalPlants : getString(R.string.not_available)));
                textViewModeOfPreparation.setText(getString(R.string.mode_of_preparation) + (modeOfPreparation != null ? modeOfPreparation : getString(R.string.not_available)));
                textViewScientificName.setText(getString(R.string.scientific_name) + (scientificName != null ? scientificName : getString(R.string.not_available)));
                textViewYearsUsedSince.setText(getString(R.string.years_used_since) + (yearsUsedSince != null ? String.valueOf(yearsUsedSince) : getString(R.string.not_available)));


                // Fetch image URLs
                fetchImageUrls(snapshot.child("imageUrls"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DrugDetailsActivity.this, "Failed to load drug details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchImageUrls(DataSnapshot imageUrlsSnapshot) {
        for (DataSnapshot snapshot : imageUrlsSnapshot.getChildren()) {
            String imageUrl = snapshot.getValue(String.class);
            Log.d(TAG, "fetchImageUrls: "+ imageUrl.toString());
            imageUrls.add(imageUrl);
        }
        imageAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back button press
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
