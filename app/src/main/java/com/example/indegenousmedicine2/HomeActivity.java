package com.example.indegenousmedicine2;
import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        Button userTile = findViewById(R.id.userTile);
        Button drugTile = findViewById(R.id.drugTile);
        Button newUserTile = findViewById(R.id.newUserTile);
        Button newDrugTile = findViewById(R.id.newDrugTile);
        Button identifyLeafTile = findViewById(R.id.identifyLeafTile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Agroषधि");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        toolbar.setLogo(R.drawable.logo_final);
        ViewGroup.LayoutParams lp = toolbar.getLayoutParams();
        if (lp instanceof Toolbar.LayoutParams) {
            ((Toolbar.LayoutParams) lp).gravity = android.view.Gravity.START;
        }
        toolbar.setLayoutParams(lp);

        userTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, UsersActivity.class));
            }
        });

        drugTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, DrugActivity.class));
            }
        });

        newUserTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, UserInfoActivity.class));
            }
        });

        newDrugTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, DrugDetails.class));
            }
        });

        identifyLeafTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, LeafPredictionActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_profile) {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            return true;
        } else if (item.getItemId() == R.id.menu_english) {
            setLocale("en");
            recreateActivity();
            return true;
        } else if (item.getItemId() == R.id.menu_assamese) {
            setLocale("as");
            recreateActivity();
            return true;
        } else if (item.getItemId() == R.id.menu_users) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent = new Intent(HomeActivity.this, UsersActivity.class);
            intent.putExtra("userName", currentUser.getDisplayName());
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_drug) {
            Log.d(TAG, "onOptionsItemSelected: Drug option selected");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setLocale(String languageCode) {
        LanguageManager.setLanguage(this, languageCode);
        LanguageManager.applyLanguage(this);
    }

    private void recreateActivity() {
        HomeActivity.this.recreate();
    }
}
