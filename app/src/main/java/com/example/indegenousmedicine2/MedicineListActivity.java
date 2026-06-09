package com.example.indegenousmedicine2;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import java.util.ArrayList;
import java.util.List;
import static android.content.ContentValues.TAG;

public class MedicineListActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    private RecyclerView recyclerViewDrugs;
    private MedicineAdapter drugAdapter;
    private List<Medicine> drugList = new ArrayList<>();
    private DatabaseReference mMessageDatabaseReference;
    private String currentUser;
    private ValueEventListener drugsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_list);

        // Initialize RecyclerView
        recyclerViewDrugs = findViewById(R.id.recyclerViewDrugs);
        recyclerViewDrugs.setLayoutManager(new LinearLayoutManager(this));
        drugAdapter = new MedicineAdapter(this, drugList);
        recyclerViewDrugs.setAdapter(drugAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.drugs_added);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize Firebase Database reference
        mMessageDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Get current user details from intent
        currentUser = getIntent().getStringExtra("CURRENT_USER");
        Log.d(TAG, "onCreate: "+ currentUser);

        // Fetch drugs from database
        fetchDrugsForCurrentUser();
    }

    private void fetchDrugsForCurrentUser() {
        drugsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                drugList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    String name = snapshot.child("Drug Name").getValue(String.class);
                    String scientificName = snapshot.child("scientificName").getValue(String.class);
                    drugList.add(new Medicine(key, name, scientificName));
                }
                drugAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MedicineListActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        mMessageDatabaseReference.child("drug_to_be_validated")
                .orderByChild("Aarogya Mitra")
                .equalTo(currentUser)
                .addValueEventListener(drugsListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (drugsListener != null) {
            mMessageDatabaseReference.child("drug_to_be_validated")
                    .orderByChild("Aarogya Mitra")
                    .equalTo(currentUser)
                    .removeEventListener(drugsListener);
        }
    }

}
