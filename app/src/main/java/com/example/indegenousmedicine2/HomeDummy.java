package com.example.indegenousmedicine2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;


import java.util.ArrayList;
import java.util.List;

public class HomeDummy extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }
    private static final String TAG = "HomeDummy";

    private ViewPager viewPager;
        private WormDotsIndicator dotsIndicator;
        private ViewPagerAdapter viewPagerAdapter;
        private ImageView languageIcon;
        private ImageView profileIcon;
        // QS tile click targets (moved to outer LinearLayout, so View — not ImageView)
        private View newUserIcon;
        private View newDrugIcon;
        private View durgListIcon;
        private View statusIconQS;
        private View drugListQS2;
        private View identifyLeafIcon;
        // Footer icons stay as ImageView (IDs remain on the ImageView in the layout)
        private ImageView usersListIcon;
        private ImageView statusIcon;
        private ImageView transactionsIcon;
        private ImageView addDrugIcon;
        private ImageView newsletterIcon;
        // Hero card
        private TextView heroName;
        private TextView heroPendingCount;
        private Query pendingCountQuery;
        private SearchView searchView;
        private List<String> featureList;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            //Forcing the Light theme to be always on
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home_dummy);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            // Hero card
            heroName = findViewById(R.id.heroName);
            heroPendingCount = findViewById(R.id.heroPendingCount);
            FirebaseUser heroUser = FirebaseAuth.getInstance().getCurrentUser();
            if (heroUser != null) {
                String displayName = heroUser.getDisplayName();
                heroName.setText(getString(R.string.hero_greeting,
                        (displayName != null && !displayName.isEmpty()) ? displayName : "there"));
            }

            // ViewPager setup
            viewPager = findViewById(R.id.viewPager);
            dotsIndicator = findViewById(R.id.dots_indicator);
            searchView = findViewById(R.id.search_view);

            initializeFeatureList();
            setupSearchView();

            List<ViewPagerItem> items = new ArrayList<>();
            items.add(new ViewPagerItem(
                    getString(R.string.authentication),
                    getString(R.string.auth_features),
                    R.drawable.ic_authentication // Replace with actual drawable resource
            ));
            items.add(new ViewPagerItem(
                    getString(R.string.language_change),
                    getString(R.string.language_change_features),
                    R.drawable.man // Replace with actual drawable resource
            ));
            items.add(new ViewPagerItem(
                    getString(R.string.profile_section),
                    getString(R.string.profile_section_features),
                    R.drawable.user // Replace with actual drawable resource
            ));
            items.add(new ViewPagerItem(
                    getString(R.string.drug_list),
                    getString(R.string.drug_list_features),
                    R.drawable.list // Replace with actual drawable resource
            ));

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            viewPagerAdapter = new ViewPagerAdapter(items);
            viewPager.setAdapter(viewPagerAdapter);
            dotsIndicator.setViewPager(viewPager);

            // Set an OnClickListener on the Language icon
            languageIcon = findViewById(R.id.language_icon);
            languageIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeDummy.this, LanguageSelectionActivity.class);
                    startActivity(intent);
                }
            });

            // Set an OnClickListener on the Profile icon
            profileIcon = findViewById(R.id.profile_icon);
            profileIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeDummy.this, ProfileActivity.class);
                    startActivity(intent);
                }
            });


            // Set an OnClickListener on the new User icon
            newUserIcon = findViewById(R.id.new_user_QS);
            newUserIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeDummy.this, UserInfoActivityDummy.class);
                    startActivity(intent);
                }
            });

            // Set an OnClickListener on the new Drug icon
            newDrugIcon = findViewById(R.id.new_drug_QS);
            newDrugIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeDummy.this, DrugDetails.class);
                    startActivity(intent);
                }
            });

            // Set an OnClickListener on the Drug List icon
            durgListIcon = findViewById(R.id.drug_list_QS);
            durgListIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (currentUser != null) {
                        // Get the user ID
                        String currentUserId = currentUser.getDisplayName();
                        Log.d(TAG, "onClick: "+ currentUserId);
                        // Create the intent
                        Intent intent = new Intent(HomeDummy.this, MedicineListActivity.class);
                        // Pass the current user ID to the intent
                        intent.putExtra("CURRENT_USER", currentUserId);
                        // Start the MedicineListActivity
                        startActivity(intent);
                    } else {
                        // Handle the case when there is no user logged in
                        // For example, redirect to the login screen or show a message
                    }
                }
            });

            // Set an OnClickListener on the Users List icon
            usersListIcon = findViewById(R.id.users_list_icon);
            usersListIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentUser!=null){
                        Intent intent = new Intent(HomeDummy.this, PeopleListActivity.class);
                        startActivity(intent);
                    }else{
                        Log.d(TAG, "onClick: User not logged in");
                    }

                }
            });

            // Set an OnClickListener on the Status icon
            statusIcon = findViewById(R.id.status_icon);
            statusIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeDummy.this, StatusActivity.class);
                    startActivity(intent);
                }
            });

            // Set an OnClickListener on the Status icon
            newsletterIcon = findViewById(R.id.newsletter_icon);
            newsletterIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeDummy.this, StatusActivity.class);
                    startActivity(intent);
                }
            });

            // Set an OnClickListener on the Status icon for Quick Search
            statusIconQS = findViewById(R.id.status_icon_QS);
            statusIconQS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeDummy.this, StatusActivity.class);
                    startActivity(intent);
                }
            });

            // Set an OnClickListener on the Transactions icon
            transactionsIcon = findViewById(R.id.transations_icon);
            transactionsIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeDummy.this, TransactionsActivity.class);
                    startActivity(intent);
                }
            });

            drugListQS2 = findViewById(R.id.list_of_plants_QS);
            drugListQS2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start DrugDetailActivity when the ImageView is clicked
                    Intent intent = new Intent(HomeDummy.this, DrugDetailActivity.class);
                    startActivity(intent);
                }
            });

            // Set an OnClickListener on the Add Drug icon
            addDrugIcon = findViewById(R.id.add_drug_icon);
            addDrugIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeDummy.this, DrugDetails.class);
                    startActivity(intent);
                }
            });

            // Wire Identify Leaf icon to LeafPredictionActivity
            identifyLeafIcon = findViewById(R.id.identify_leaf_QS);
            identifyLeafIcon.setOnClickListener(v -> {
                startActivity(new Intent(HomeDummy.this, LeafPredictionActivity.class));
            });

            loadPendingCount();
        }

    private void loadPendingCount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || heroPendingCount == null) return;
        String userName = user.getDisplayName() != null ? user.getDisplayName() : "";
        pendingCountQuery = FirebaseDatabase.getInstance().getReference()
                .child("drug_to_be_validated")
                .orderByChild("Aarogya Mitra")
                .equalTo(userName);
        pendingCountQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                heroPendingCount.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { /* no-op */ }
        });
    }

    private void initializeFeatureList() {
        featureList = new ArrayList<>();
        featureList.add(getString(R.string.authentication));
        featureList.add(getString(R.string.language_change));
        featureList.add(getString(R.string.profile_section));
        featureList.add(getString(R.string.drug_list));
        }

    private void setupSearchView() {
        // Get a reference to the SearchView
        SearchView searchView = findViewById(R.id.search_view);

        // Set a query text listener to listen for changes in the search query
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search query submission
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle changes in the search query text
                // You can perform real-time filtering or suggestions here
                performSearch(newText);
                return true;
            }
        });
    }
    private void performSearch(String query) {
        // Filter the list of features based on the query
        List<String> filteredList = new ArrayList<>();
        for (String feature : featureList) {
            if (feature.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(feature);
            }
        }

        // Update the UI to display the filtered results
        // For example, you can update a RecyclerView or ListView with the filtered results
        // Here, let's just log the filtered results
        Log.d("Filtered Results", filteredList.toString());
    }

}








