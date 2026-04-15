package com.example.indegenousmedicine2;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class DrugActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textViewNoData;
    private DrugAdapter adapter;
    private List<Drug> drugList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug);

        // Retrieve current user's name from previous activity
        String currentUserName = getIntent().getStringExtra("currentUserName");

        recyclerView = findViewById(R.id.recyclerView);
        textViewNoData = findViewById(R.id.textViewNoData);

        // Initialize RecyclerView and Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        drugList = new ArrayList<>();
        adapter = new DrugAdapter(this, drugList);
        recyclerView.setAdapter(adapter);

        // Query Firebase to fetch drugs associated with the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("drugs");
        Query query = databaseReference.orderByChild("user").equalTo(currentUserName);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                drugList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Drug drug = snapshot.getValue(Drug.class);
                    if (drug != null) {
                        drugList.add(drug);
                    }
                }
                adapter.notifyDataSetChanged();
                if (drugList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    textViewNoData.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    textViewNoData.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });

        // Set item click listener for RecyclerView
        adapter.setOnItemClickListener(new DrugAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Open Drug Details Activity for selected drug
                Drug selectedDrug = drugList.get(position);
                // Start DrugDetailsActivity with selectedDrug details
                // Intent intent = new Intent(DrugActivity.this, DrugDetailsActivity.class);
                // Pass selectedDrug details to DrugDetailsActivity
                // intent.putExtra("selectedDrug", selectedDrug);
                // startActivity(intent);
            }
        });
    }
}
