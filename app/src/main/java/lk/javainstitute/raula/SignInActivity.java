package lk.javainstitute.raula;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lk.javainstitute.raula.model.ForgotPasswordBottomSheet;

public class SignInActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private static final String PREFS_NAME = "UserPrefs";
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        auth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        progressBar = findViewById(R.id.progressBar6);
        TextView signUp = findViewById(R.id.signup_nav_btn);
        signUp.setOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });
        TextView forgetPassword = findViewById(R.id.forgetPassword_btn);
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ForgotPasswordBottomSheet bottomSheet = new ForgotPasswordBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "ForgotPasswordBottomSheet");
            }
        });


        Button loginBtn = findViewById(R.id.button);
        loginBtn.setOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            loginVerification();
        });



        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
    }



    private void loginVerification() {
        TextInputLayout emailInput = findViewById(R.id.emailInputLayout);
        TextInputLayout passwordInput = findViewById(R.id.passwordInputLayout1);

        String email = emailInput.getEditText().getText().toString().trim();
        String passwordEntry = passwordInput.getEditText().getText().toString().trim();

        if (email.isEmpty()) {  // Changed isBlank() to isEmpty() for compatibility
            Toast.makeText(this, "Enter Your Email!", Toast.LENGTH_SHORT).show();
        } else if (passwordEntry.isEmpty()) {
            Toast.makeText(this, "Enter Your Password!", Toast.LENGTH_SHORT).show();
        } else {
            loginCheck(email, passwordEntry);
        }
    }

    private void loginCheck(String email, String passwordEntry) {
        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("app_users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", passwordEntry)  // Not secure! Consider hashing passwords.
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userId = document.getId();  // Get user ID from Firestore
                            saveUserSession(userId);  // Save session
                            goToHome();
                        }
                    } else {
                        Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Login Failed! Check your internet connection.", Toast.LENGTH_SHORT).show());
    }



    private void saveUserSession(String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId); // Save user ID for future use
        editor.putBoolean("isRegistered", true);
        editor.putBoolean("isVerified", true);
        editor.apply();
    }

    private void goToHome() {
        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
