package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private TextInputEditText password;
    private Button loginBtn, googleBtn;
    private TextView signupText, forgetPass;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // --- GOOGLE LOGIN VARIABLES ---
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Firebase Initialization
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 2. Bind Views
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        googleBtn = findViewById(R.id.googleBtn);
        signupText = findViewById(R.id.signupText);
        forgetPass = findViewById(R.id.forgetPass);

        // --- 3. GOOGLE SIGN-IN SETUP ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleBtn.setOnClickListener(v -> signInWithGoogle());

        // 4. Email/Password Login Button Click
        loginBtn.setOnClickListener(v -> {
            String inputEmail = email.getText().toString().trim();
            String inputPass = password.getText().toString().trim();

            if (TextUtils.isEmpty(inputEmail) || TextUtils.isEmpty(inputPass)) {
                Toast.makeText(this, "Please enter all details!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (inputEmail.equalsIgnoreCase("admin@skillswap.com") && inputPass.equals("admin123")) {
                handleAdminLogin();
                return;
            }

            loginViaDatabase(inputEmail, inputPass);
        });

        signupText.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        forgetPass.setOnClickListener(v -> startActivity(new Intent(this, ForgetPasswordActivity.class)));
    }

    // --- GOOGLE SIGN-IN METHODS ---
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    syncGoogleUserToDatabase(user);
                }
            } else {
                Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncGoogleUserToDatabase(FirebaseUser user) {
        mDatabase.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("name", user.getDisplayName());
                    userMap.put("email", user.getEmail());
                    userMap.put("uid", user.getUid());
                    userMap.put("avatarId", 0);
                    userMap.put("needsReset", false);

                    mDatabase.child("Users").child(user.getUid()).setValue(userMap);
                }

                saveUserSession(user.getUid(), user.getEmail());
                Toast.makeText(LoginActivity.this, "Signed in as " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // --- EXISTING LOGIN METHODS ---
    private void loginViaDatabase(String inputEmail, String inputPass) {
        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");

        mDatabase.child("Users").orderByChild("email").equalTo(inputEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Login");

                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                String dbPassword = userSnap.child("password").getValue(String.class);
                                String userId = userSnap.getKey();
                                Boolean needsReset = userSnap.child("needsReset").getValue(Boolean.class);

                                if (dbPassword != null && dbPassword.equals(inputPass)) {
                                    saveUserSession(userId, inputEmail);
                                    mAuth.signInWithEmailAndPassword(inputEmail, inputPass);

                                    if (Boolean.TRUE.equals(needsReset)) {
                                        Intent resetIntent = new Intent(LoginActivity.this, CreateNewPasswordActivity.class);
                                        startActivity(resetIntent);
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Welcome Back!", Toast.LENGTH_SHORT).show();
                                        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(homeIntent);
                                    }
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Invalid Password!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Email not registered!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Login");
                    }
                });
    }

    private void saveUserSession(String id, String email) {
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userId", id);
        editor.putString("userEmail", email);
        editor.apply();
    }

    private void handleAdminLogin() {
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        sp.edit().putBoolean("isLoggedIn", true).putString("role", "admin").apply();
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}