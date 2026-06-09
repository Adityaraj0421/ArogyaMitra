package com.example.indegenousmedicine2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class LanguageSelectionActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    private TextView textLanguageSelection;
    private RadioGroup radioGroupLanguage;
    private RadioButton radioEnglish, radioAssamese;
    private Button btnSelectLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        textLanguageSelection = findViewById(R.id.text_language_selection);
        radioGroupLanguage = findViewById(R.id.radio_group_language);
        radioEnglish = findViewById(R.id.radio_english);
        radioAssamese = findViewById(R.id.radio_assamese);
        btnSelectLanguage = findViewById(R.id.btn_select_language);

        // Apply slide up animation to the title
        Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        textLanguageSelection.startAnimation(slideUpAnimation);

        // Apply fade in animation to the button
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        btnSelectLanguage.startAnimation(fadeInAnimation);

        btnSelectLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedLanguage = radioEnglish.isChecked() ? "en" : "as";
                setLocale(selectedLanguage);
                goToNextActivity();
            }
        });
    }

    private void setLocale(String languageCode) {
        LanguageManager.setLanguage(this, languageCode);
        LanguageManager.applyLanguage(this);
    }

    private void goToNextActivity() {
        Intent intent = new Intent(this, HomeDummy.class);
        startActivity(intent);
        finish();
    }
}
