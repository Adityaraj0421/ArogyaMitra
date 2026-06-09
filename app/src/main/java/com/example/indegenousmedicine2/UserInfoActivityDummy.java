package com.example.indegenousmedicine2;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;;import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserInfoActivityDummy extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    private Button buttonPersonalDetails;
    private Button buttonAddress;
    private Button buttonContactInfo;
    private UserInfoViewModel userInfoViewModel;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_dummy);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        userInfoViewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Peoples");

        buttonPersonalDetails = findViewById(R.id.buttonPersonalDetails);
        buttonAddress = findViewById(R.id.buttonAddress);
        buttonContactInfo = findViewById(R.id.buttonContactInfo);

        // Load the initial fragment
        loadFragment(new PersonalDetailsFragment());
        highlightButton(buttonPersonalDetails);

        buttonPersonalDetails.setOnClickListener(v -> {
            loadFragment(new PersonalDetailsFragment());
            highlightButton(buttonPersonalDetails);
        });

        buttonAddress.setOnClickListener(v -> {
            loadFragment(new AddressFragment());
            highlightButton(buttonAddress);
        });

        buttonContactInfo.setOnClickListener(v -> {
            loadFragment(new ContactInfoFragment());
            highlightButton(buttonContactInfo);
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void highlightButton(Button buttonToHighlight) {
        // Reset all button backgrounds to the default color
        buttonPersonalDetails.setBackgroundColor(Color.parseColor("#FF4081"));
        buttonAddress.setBackgroundColor(Color.parseColor("#FF4081"));
        buttonContactInfo.setBackgroundColor(Color.parseColor("#FF4081"));

        // Set the highlighted button background to a lighter color
        buttonToHighlight.setBackgroundColor(Color.parseColor("#FF80AB"));
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

//    @Override
//    public void onBackPressed() {
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        if (fragmentManager.getBackStackEntryCount() > 1) {
//            fragmentManager.popBackStack();
//        } else {
//            finish();
//        }
//    }

    public void displayUserInfo() {
        UserInfo userInfo = userInfoViewModel.getUserInfo();
//        String message = "Name: " + userInfo.getName() +
//                "\nAge: " + userInfo.getAge() +
//                "\nAadhar: " + userInfo.getAadharNumber() +
//                "\nAddress: " + userInfo.getAddress() +
//                "\nPhone: " + userInfo.getPhoneNumber() +
//                "\nPAN: " + userInfo.getPanNumber();
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currUser = (currentUser != null) ? currentUser.getDisplayName() : "";
        Log.d(TAG, "addUser: "+ currUser);
        databaseReference.push().setValue(userInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Show success toast message
                            Toast.makeText(UserInfoActivityDummy.this, "User added successfully", Toast.LENGTH_SHORT).show();
                            // Redirect to MainActivity
                            // Create an Intent to start the next activity
                            Intent intent = new Intent(UserInfoActivityDummy.this, HomeDummy.class);
                            intent.putExtra("NameOfClient", userInfo.getName());

// Start the next activity with the modified Intent
                            startActivity(intent);
                            finish();
                        } else {
                            // Show error toast message
                            Toast.makeText(UserInfoActivityDummy.this, "Failed to add user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
