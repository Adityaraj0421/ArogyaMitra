package com.example.indegenousmedicine2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class TransactionsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "agro_prefs";
    private static final String KEY_SUBSCRIBED = "is_subscribed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageManager.applyLanguage(this);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isSubscribed = prefs.getBoolean(KEY_SUBSCRIBED, false);

        if (!isSubscribed) {
            startActivity(new Intent(this, SubscriptionActivity.class));
            finish();
            return;
        }

        // Already subscribed — show confirmation screen
        setContentView(R.layout.activity_transactions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.subscription_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        TextView tv = findViewById(R.id.textViewNothingHere);
        if (tv != null) tv.setText(R.string.subscription_active);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
