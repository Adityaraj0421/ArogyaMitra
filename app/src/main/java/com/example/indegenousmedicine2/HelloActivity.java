//package com.example.indegenousmedicine2;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.animation.AlphaAnimation;
//import android.view.animation.Animation;
//import android.view.animation.AnimationSet;
//import android.view.animation.ScaleAnimation;
//import android.widget.TextView;
//
//public class HelloActivity extends AppCompatActivity {
//
//    private static final int SPLASH_DELAY = 5000; // 5 seconds
//    private static final String PREFS_NAME = "MyPrefsFile";
//    private static final String FIRST_TIME_KEY = "isFirstTime";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_hello);
//
//        // Check if it's the first time opening the app
//        if (isFirstTime()) {
//            // Set isFirstTime to false so that next time it won't be considered as first time
//            setFirstTime(false);
//        }
//
////        TextView appNameTextView = findViewById(R.id.app_name);
//        TextView taglineTextView = findViewById(R.id.tagline);
//
//        // Animations
//        Animation fadeIn = new AlphaAnimation(0, 1);
//        fadeIn.setDuration(2000);
//        Animation scaleAnimation = new ScaleAnimation(0.5f, 1f, 0.5f, 1f);
//        scaleAnimation.setDuration(2000);
//
//        AnimationSet animationSet = new AnimationSet(true);
//        animationSet.addAnimation(fadeIn);
//        animationSet.addAnimation(scaleAnimation);
//
////        appNameTextView.setAnimation(animationSet);
//        taglineTextView.setAnimation(animationSet);
//
//        // Delay to start next activity
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // If the app is opened for the first time, go to LanguageSelectionActivity
//                // Otherwise, go to MainActivity
//                Intent intent;
//                if (isFirstTime()) {
////                    intent = new Intent(HelloActivity.this, MainActivity.class);
////                    intent = new Intent(HelloActivity.this, LanguageSelectionActivity.class);
//                    intent = new Intent(HelloActivity.this, HomeDummy.class);
//                } else {
////                    intent = new Intent(HelloActivity.this, MainActivity.class);
////                    intent = new Intent(HelloActivity.this, LanguageSelectionActivity.class);
//                    intent = new Intent(HelloActivity.this, HomeDummy.class);
//                }
//                startActivity(intent);
//                finish();
//            }
//        }, SPLASH_DELAY);
//    }
//
//    // Method to check if the app is opened for the first time
//    private boolean isFirstTime() {
//        // You can implement your logic here to check if the app is opened for the first time
//        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        return sharedPref.getBoolean(FIRST_TIME_KEY, true);
//    }
//
//    // Method to set isFirstTime flag to false
//    private void setFirstTime(boolean isFirstTime) {
//        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putBoolean(FIRST_TIME_KEY, isFirstTime);
//        editor.apply();
//    }
//}

package com.example.indegenousmedicine2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import static android.content.ContentValues.TAG;

public class HelloActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    private static final int SPLASH_DELAY = 5000; // 5 seconds
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String IS_LOGGED_IN_KEY = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        TextView taglineTextView = findViewById(R.id.tagline);

        // Animations
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(2000);
        Animation scaleAnimation = new ScaleAnimation(0.5f, 1f, 0.5f, 1f);
        scaleAnimation.setDuration(2000);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(fadeIn);
        animationSet.addAnimation(scaleAnimation);

        taglineTextView.setAnimation(animationSet);

        // Delay to start next activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (isLoggedIn()) {
                    Log.d(TAG, "run: isLoggedIn");
                    intent = new Intent(HelloActivity.this, HomeDummy.class);
                } else {
                    Log.d(TAG, "run: notLoggedIn");
                    intent = new Intent(HelloActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, SPLASH_DELAY);
    }

    // Method to check if the user is logged in
    private boolean isLoggedIn() {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(IS_LOGGED_IN_KEY, false);
    }
}
