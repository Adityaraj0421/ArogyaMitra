package com.example.indegenousmedicine2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DrugListActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    public static final String EXTRA_PLANT_QUERY = "PLANT_QUERY";

    /** Owner tag used by the seeded plant catalogue (firebase_seed/import_seed.js). */
    private static final String SEED_CATALOGUE_OWNER = "Seed Catalogue";

    private RecyclerView recyclerViewDrugs;
    private android.widget.TextView textViewEmpty;
    private ArrayList<Medicine> medicines;
    private MedicineAdapter adapter;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private ValueEventListener drugsListener;
    private Query drugsQuery;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        retrieveDrugsForCurrentUser(currentUser);
                    } else {
                        Toast.makeText(this, "Failed to sign in.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    IdpResponse response = IdpResponse.fromResultIntent(result.getData());
                    if (response != null) {
                        Toast.makeText(this, "Sign in failed: " + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Sign in failed.", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_list);
        LanguageManager.applyLanguage(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerViewDrugs = findViewById(R.id.recyclerViewDrugs);
        textViewEmpty = findViewById(R.id.textViewEmptyDrugs);
        recyclerViewDrugs.setLayoutManager(new LinearLayoutManager(this));

        medicines = new ArrayList<>();
        adapter = new MedicineAdapter(this, medicines);
        recyclerViewDrugs.setAdapter(adapter);

        String plantQuery = getIntent().getStringExtra(EXTRA_PLANT_QUERY);
        if (plantQuery != null && !plantQuery.isEmpty()) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(plantQuery);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            retrieveDrugsByPlantName(plantQuery);
        } else {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                signInWithGoogle();
            } else {
                retrieveDrugsForCurrentUser(currentUser);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LanguageManager.applyLanguage(this);
    }

    private void signInWithGoogle() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build());
        signInLauncher.launch(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build());
    }

    private void retrieveDrugsForCurrentUser(FirebaseUser user) {
        String userName = user.getDisplayName();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("drug_to_be_validated");
        // Show the seeded catalogue plus anything THIS user submitted. Firebase has
        // no server-side OR, so read the node and filter on the owner tag client-side.
        drugsQuery = databaseReference;

        drugsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                medicines.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String owner = snapshot.child("Aarogya Mitra").getValue(String.class);
                    boolean isCatalogue = SEED_CATALOGUE_OWNER.equals(owner);
                    boolean isMine = userName != null && userName.equals(owner);
                    if (!isCatalogue && !isMine) continue;

                    String key = snapshot.getKey();
                    String drugName = snapshot.child("Drug Name").getValue(String.class);
                    String scientificName = snapshot.child("scientificName").getValue(String.class);
                    medicines.add(new Medicine(key, drugName, scientificName));
                }
                adapter.notifyDataSetChanged();
                boolean empty = medicines.isEmpty();
                recyclerViewDrugs.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
                textViewEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DrugListActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        drugsQuery.addValueEventListener(drugsListener);
    }

    private void retrieveDrugsByPlantName(String plantQuery) {
        List<String> keywords = extractKeywords(plantQuery);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("drug_to_be_validated");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                medicines.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    String drugName = snapshot.child("Drug Name").getValue(String.class);
                    String scientificName = snapshot.child("scientificName").getValue(String.class);
                    if (drugMatchesQuery(drugName, keywords) || drugMatchesQuery(scientificName, keywords)) {
                        medicines.add(new Medicine(key, drugName, scientificName));
                    }
                }
                adapter.notifyDataSetChanged();
                if (medicines.isEmpty()) {
                    Toast.makeText(DrugListActivity.this, R.string.no_drugs_available, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DrugListActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> extractKeywords(String label) {
        List<String> keywords = new ArrayList<>();
        String lower = label.toLowerCase(Locale.US);
        keywords.add(lower);
        int start = label.indexOf('(');
        int end = label.indexOf(')');
        if (start >= 0 && end > start) {
            keywords.add(label.substring(start + 1, end).trim().toLowerCase(Locale.US));
            keywords.add(label.substring(0, start).trim().toLowerCase(Locale.US));
        }
        return keywords;
    }

    private boolean drugMatchesQuery(String field, List<String> keywords) {
        if (field == null) return false;
        String lower = field.toLowerCase(Locale.US);
        for (String kw : keywords) {
            if (lower.contains(kw) || kw.contains(lower)) return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (drugsQuery != null && drugsListener != null) {
            drugsQuery.removeEventListener(drugsListener);
        }
    }
}
