//package com.example.indegenousmedicine2;
//
//import static android.content.ContentValues.TAG;
//
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GoogleAuthProvider;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private static final int RC_SIGN_IN = 123;
//    private FirebaseAuth mAuth;
//    private GoogleSignInClient mGoogleSignInClient;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        mAuth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if (currentUser != null) {
//            // User is already signed in, redirect to MainActivity
//            startActivity(new Intent(LoginActivity.this, HomeDummy.class));
//            finish();
//        }
//
//        // Configure Google Sign In
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        // Start the Google Sign-In Intent
//        signIn();
//
//        Button signInButton = findViewById(R.id.btn_google_signin);
//        signInButton.setOnClickListener(v -> signIn());
//    }
//
//    private void signIn() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account.getIdToken());
//            } catch (ApiException e) {
//                // Google Sign In failed
//                Log.w(TAG, "Google sign in failed", e);
//                Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//    }
//
//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        // Sign in success, update UI with the signed-in user's information
//                        Log.d(TAG, "signInWithCredential:success");
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        if(user!=null){
//                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
//                            finish();
//                        }
//                        updateUI(user);
//                    } else {
//                        // If sign in fails, display a message to the user.
//                        Log.w(TAG, "signInWithCredential:failure", task.getException());
//                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
//                        updateUI(null);
//                    }
//                });
//    }
//
//    private void updateUI(FirebaseUser user) {
//        if (user != null) {
//            // User is signed in, redirect to MainActivity
//            startActivity(new Intent(LoginActivity.this, HomeDummy.class));
//            finish();
//        } else {
//            // User is not signed in, stay on LoginActivity or handle the flow accordingly
//            // For example, show an error message or provide the option to retry login
//        }
//    }
//}

package com.example.indegenousmedicine2;

import static android.content.ContentValues.TAG;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    private static final int RC_SIGN_IN = 123;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String IS_LOGGED_IN_KEY = "isLoggedIn";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ImageView loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is already signed in, redirect to HomeActivity
            Log.d(TAG, "onCreate: insideLodinActivity user Loggedin");
            startActivity(new Intent(LoginActivity.this, HomeDummy.class));
            finish();
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button signInButton = findViewById(R.id.btn_google_signin);
        loadingAnimation = findViewById(R.id.loading_animation);

        signInButton.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        // Start loading animation
        loadingAnimation.setVisibility(View.VISIBLE);
        AnimationDrawable animation = (AnimationDrawable) loadingAnimation.getDrawable();
        animation.start();

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                loadingAnimation.setVisibility(View.GONE); // Hide animation on failure
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveLoginState(true);
                            navigateToHome();
                        }
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        loadingAnimation.setVisibility(View.GONE); // Hide animation on failure
                        updateUI(null);
                    }
                });
    }

    private void saveLoginState(boolean isLoggedIn) {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(IS_LOGGED_IN_KEY, isLoggedIn);
        editor.apply();
    }

    private void navigateToHome() {
        Log.d(TAG, "navigateToHome: user not logged in, now got login");
        Intent intent = new Intent(LoginActivity.this, HomeDummy.class);
        startActivity(intent);
        finish();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in, redirect to HomeActivity
            startActivity(new Intent(LoginActivity.this, HomeDummy.class));
            finish();
        } else {
            // User is not signed in, stay on LoginActivity or handle the flow accordingly
            // For example, show an error message or provide the option to retry login
        }
    }
}

