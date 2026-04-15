package com.example.indegenousmedicine2;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class UserInfoActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextAge;
    private EditText editTextAreaSince;
    private EditText editTextAadhar;
    private EditText editTextPhoneNumber;
    private EditText editTextAddress;
    private EditText editTextPan;
    private Button buttonSubmit;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // Apply language when activity is created
        LanguageManager.applyLanguage(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Agroषधि");
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextAreaSince = findViewById(R.id.editTextAreaSince);
        editTextAadhar = findViewById(R.id.editTextAadhar);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPan = findViewById(R.id.editTextPan);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Peoples");

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUser();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Apply language when activity is resumed
        LanguageManager.applyLanguage(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_language, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { // Check if the back button is pressed
            // Redirect to UserInfoActivity
            Log.d(TAG, "onOptionsItemSelected: userInfo");
            startActivity(new Intent(UserInfoActivity.this, HomeActivity.class));
            return true;
        }else if (item.getItemId() == R.id.menu_profile) {
            // Handle profile menu item click
            startActivity(new Intent(UserInfoActivity.this, ProfileActivity.class));
//            Log.d(TAG, package com.example.indegenousmedicine2;);
            return true;
        }else if (item.getItemId() == R.id.menu_english) {
            setLocale("en");
            recreateActivity();
//            LanguageManager.setLanguage(this, "en");
//            LanguageManager.applyLanguage(this);
            return true;
        } else if (item.getItemId() == R.id.menu_assamese) {
            setLocale("as");
            recreateActivity();
//            LanguageManager.setLanguage(this, "as");
//            LanguageManager.applyLanguage(this);
            return true;
        } else if (item.getItemId() == R.id.menu_users) {
                // Handle profile menu item click
//                startActivity(new Intent(UserInfoActivity.this, DrugActivity.class));
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            Intent intent = new Intent(UserInfoActivity.this, UsersActivity.class);
            if (currentUser != null) {
                intent.putExtra("userName", currentUser.getDisplayName());
            }
            startActivity(intent);
//            Log.d(TAG, package com.example.indegenousmedicine2;);
                return true;
            }else{
            return super.onOptionsItemSelected(item);
        }
    }


    private void addUser() {
        String name = editTextName.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();
        String areaSince = editTextAreaSince.getText().toString().trim();
        String aadhar = editTextAadhar.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String pan = editTextPan.getText().toString().trim();

        // Check if Aadhar and Phone number are of the correct lengths
//        if (aadhar.length() != 16) {
//            editTextAadhar.setError("Aadhar number must be 16 digits long");
//            editTextAadhar.requestFocus();
//            return;
//        }

//        if (phoneNumber.length() != 10) {
//            editTextPhoneNumber.setError("Phone number must be 10 digits long");
//            editTextPhoneNumber.requestFocus();
//            return;
//        }

        // Create a new User object
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currUser = currentUser.getDisplayName();
        Log.d(TAG, "addUser: "+ currUser);
        User user = new User(currUser, name, age, areaSince, aadhar, phoneNumber, address, pan);

        // Push the user to the database under "Peoples" node
//        databaseReference.push().setValue(user);
//
//        // Redirect to MainActivity
//        startActivity(new Intent(UserInfoActivity.this, MainActivity.class));
//        finish();

        databaseReference.push().setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Show success toast message
                            Toast.makeText(UserInfoActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
                            // Redirect to MainActivity
                            // Create an Intent to start the next activity
                            Intent intent = new Intent(UserInfoActivity.this, DrugDetails.class);
                            intent.putExtra("NameOfClient", name);

// Start the next activity with the modified Intent
                            startActivity(intent);
                            finish();
                        } else {
                            // Show error toast message
                            Toast.makeText(UserInfoActivity.this, "Failed to add user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//        Intent userDetailsIntent = new Intent(UserInfoActivity.this, DrugDetails.class);
//        startActivity(userDetailsIntent);
//        finish();
    }

    private void setLocale(String languageCode) {
            // Store the selected language preference in SharedPreferences
            LanguageManager.setLanguage(this, languageCode);
            LanguageManager.applyLanguage(this);
    }

    private void recreateActivity() {
        // Recreate the activity to apply the new language
        UserInfoActivity.this.recreate();
    }
}
