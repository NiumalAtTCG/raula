package lk.javainstitute.raula;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class PasswordConfermActivity extends AppCompatActivity {

    private String UserId;
    private String pascodeEntry;
    private String pascode_conferm;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String USER_ID = "userId";
    private FirebaseFirestore firestore;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password_conferm);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        progressBar = findViewById(R.id.progressBar8);
        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        Button submitButton = findViewById(R.id.button);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isUserVerified = sharedPreferences.getBoolean("isVerified", false);
        UserId = sharedPreferences.getString(USER_ID, null);

        submitButton.setOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            if (isUserVerified) {
                verification();
            } else {
                Toast.makeText(PasswordConfermActivity.this, "Please Register First!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verification() {
        TextInputLayout newpasswordInput = findViewById(R.id.passwordInputLayout);
        TextInputLayout confirmpasswordInput = findViewById(R.id.passwordInputLayout1); // Fixed reference

        pascodeEntry = newpasswordInput.getEditText().getText().toString().trim();
        pascode_conferm = confirmpasswordInput.getEditText().getText().toString().trim();

        if (pascodeEntry.isEmpty()) {  // Use isEmpty() for better compatibility
            Toast.makeText(this, "Enter Your Password!", Toast.LENGTH_SHORT).show();
        } else if (pascode_conferm.isEmpty()) {
            Toast.makeText(this, "Confirm Your Password!", Toast.LENGTH_SHORT).show();
        } else if (pascodeEntry.equals(pascode_conferm)) {
            savePassword();
        } else {
            Toast.makeText(this, "Passwords Do Not Match!", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePassword() {
        if (UserId == null) {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Store the password (Consider hashing before storing for security)
        HashMap<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("password", pascodeEntry);  // Use pascodeEntry (they match in verification)
        progressBar.setVisibility(View.VISIBLE);
        // Update Firestore document
        firestore.collection("app_users")
                .document(UserId)  // Access user's document by ID
                .update(userUpdate)  // Update the password field
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PasswordConfermActivity.this, "Password Saved successfully!", Toast.LENGTH_SHORT).show();
                    goToLogin();
                })

                .addOnFailureListener(e ->
                        Toast.makeText(PasswordConfermActivity.this, "Error updating password!", Toast.LENGTH_SHORT).show());
    }

    private void goToLogin() {
        Intent intent = new Intent(PasswordConfermActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
