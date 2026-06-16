package com.example.indegenousmedicine2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DrugDetailActivity extends AppCompatActivity {

    /** Owner tag used by the seeded plant catalogue (firebase_seed/import_seed.js). */
    private static final String SEED_CATALOGUE_OWNER = "Seed Catalogue";

    private RecyclerView drugRecyclerView;
    private DrugAdapterForPlantsDetails drugAdapter;
    private List<DrugDetail> drugList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_detail);

        // Set up the toolbar with a back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            // Navigate to HomeDummy when the back button is clicked
            Intent intent = new Intent(DrugDetailActivity.this, HomeDummy.class);
            startActivity(intent);
            finish();
        });

        // Initialize RecyclerView and other variables
        drugRecyclerView = findViewById(R.id.drugRecyclerView);
        drugRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        drugList = new ArrayList<>();
        drugAdapter = new DrugAdapterForPlantsDetails(this, drugList);
        drugRecyclerView.setAdapter(drugAdapter);

        // Fetch data from Firebase
        fetchDrugDetails();
    }

    private void fetchDrugDetails() {
        // The plant catalogue lives in "drug_to_be_validated" (the 229 seeded plants
        // plus user submissions). The old code read "ListOfValues", a node that does
        // not exist in this database, so the list was always empty.
        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference().child("drug_to_be_validated");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myName = user != null ? user.getDisplayName() : null;

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                drugList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Catalogue + my records (the seeds are owned by "Seed Catalogue").
                    String owner = snapshot.child("Aarogya Mitra").getValue(String.class);
                    boolean isCatalogue = SEED_CATALOGUE_OWNER.equals(owner);
                    boolean isMine = myName != null && myName.equals(owner);
                    if (!isCatalogue && !isMine) continue;

                    // The stored field names differ from DrugDetail's @PropertyName
                    // mapping, so map them explicitly here.
                    String vernacular = snapshot.child("Drug Name").getValue(String.class);
                    if (vernacular == null) {
                        vernacular = snapshot.child("medicinalPlants").getValue(String.class);
                    }
                    String scientific = snapshot.child("scientificName").getValue(String.class);
                    String use = snapshot.child("howToApply").getValue(String.class);
                    if (use == null) {
                        use = snapshot.child("modeOfPreparation").getValue(String.class);
                    }
                    String photo = firstImageUrl(snapshot.child("imageUrls"));

                    drugList.add(new DrugDetail(scientific, vernacular, photo, use));
                }
                drugAdapter.notifyDataSetChanged();
                Log.d("DrugDetailActivity", "Loaded " + drugList.size() + " plants");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DrugDetailActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    /** Returns the best image URL under imageUrls (prefers the wiki image). */
    private String firstImageUrl(DataSnapshot imageUrls) {
        if (imageUrls == null || !imageUrls.exists()) return null;
        String wiki = imageUrls.child("wiki").getValue(String.class);
        if (wiki != null && !wiki.isEmpty()) return wiki;
        for (DataSnapshot child : imageUrls.getChildren()) {
            String url = child.getValue(String.class);
            if (url != null && !url.isEmpty()) return url;
        }
        return null;
    }
}
