package com.example.indegenousmedicine2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import static android.content.ContentValues.TAG;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PeopleListActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    private RecyclerView recyclerViewPeople;
    private android.widget.TextView textViewEmpty;
    private PeopleAdapter peopleAdapter;
    private List<String> peopleList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.users_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewPeople = findViewById(R.id.recyclerViewPeople);
        textViewEmpty = findViewById(R.id.textViewEmptyPeople);
        recyclerViewPeople.setLayoutManager(new LinearLayoutManager(this));
        peopleAdapter = new PeopleAdapter(peopleList);
        recyclerViewPeople.setAdapter(peopleAdapter);

        // Get current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUser = (firebaseUser != null) ? firebaseUser.getDisplayName() : "";

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Fetch people from the database
        fetchPeople();
    }

    private void fetchPeople() {
        Log.d(TAG, "fetchPeople: " + currentUser);
        databaseReference.child("Peoples").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                peopleList.clear();
                String defaultValue = getString(R.string.name_missing);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String addedBy = snapshot.child("user").getValue(String.class);
                    if (addedBy != null && addedBy.equals(currentUser) && name != null) {
                        if (name.length() != 0) {
                            peopleList.add(name);
                        } else {
                            peopleList.add(defaultValue);
                        }
                    }
                }
                peopleAdapter.notifyDataSetChanged();
                boolean empty = peopleList.isEmpty();
                recyclerViewPeople.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
                textViewEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back button press
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
