package lk.javainstitute.raula;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private FirebaseFirestore firestore;
    private String email, otp;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        progressBar = findViewById(R.id.progressBar7);
        firestore = FirebaseFirestore.getInstance();

        Button button = findViewById(R.id.add_profile_button);
        Button backtologinButton = findViewById(R.id.forget_password_btn);

        backtologinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
        button.setOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);



            handleSignUp();
        });
    }

    private void handleSignUp() {
        TextInputLayout fnameInput = findViewById(R.id.textInputLayout);
        TextInputLayout lnameInput = findViewById(R.id.textInputLayout2);
        TextInputLayout mobileInput = findViewById(R.id.textInputLayout3);
        TextInputLayout emailInput = findViewById(R.id.textInputLayout4);
        TextInputLayout addressInput = findViewById(R.id.textInputLayout5);

        String fname = fnameInput.getEditText().getText().toString().trim();
        String lname = lnameInput.getEditText().getText().toString().trim();
        String mobile = mobileInput.getEditText().getText().toString().trim();
        email = emailInput.getEditText().getText().toString().trim();
        String address = addressInput.getEditText().getText().toString().trim();

        if (fname.isEmpty() || lname.isEmpty() || mobile.isEmpty() || email.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mobile.matches("\\+?[0-9]{10,13}")) {
            Toast.makeText(this, "Invalid mobile number", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        // Check if user already exists
        firestore.collection("app_users")
                .where(Filter.and(Filter.equalTo("mobile", mobile), Filter.equalTo("email", email)))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(SignUpActivity.this, "User already exists!", Toast.LENGTH_SHORT).show();
                    } else {
                        registerNewUser(fname, lname, mobile, email, address);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user", e);
                    Toast.makeText(SignUpActivity.this, "Error checking user", Toast.LENGTH_SHORT).show();
                });
    }

    private void registerNewUser(String fname, String lname, String mobile, String email, String address) {
        HashMap<String, Object> user = new HashMap<>();
        user.put("fname", fname);
        user.put("lname", lname);
        user.put("mobile", mobile);
        user.put("email", email);
        user.put("address", address);
        user.put("registration_date", Timestamp.now());
        user.put("user_status", "active");
        user.put("user_type", "user");

        firestore.collection("app_users")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    String newUserId = documentReference.getId();

                    Toast.makeText(SignUpActivity.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("userId", newUserId);
                    editor.apply();

                    sendOtpToEmail(email);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding user", e);
                    Toast.makeText(SignUpActivity.this, "Error adding user", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendOtpToEmail(String email) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://5d97-112-135-71-237.ngrok-free.app/raula/send_OTP?email="+email)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String responseText = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseText).getAsJsonObject();
                otp = jsonResponse.get("content").getAsString().trim();

                Log.i(TAG, "Generated OTP: " + otp);
                runOnUiThread(() -> navigateToVerification(otp));
            } catch (Exception e) {
                Log.e(TAG, "OTP request failed", e);
                runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Failed to send OTP", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void navigateToVerification(String otp) {

        Intent intent = new Intent(SignUpActivity.this, VerificationActivity.class);
        intent.putExtra("user_OTP", otp);
        startActivity(intent);
        finish();
    }
}
