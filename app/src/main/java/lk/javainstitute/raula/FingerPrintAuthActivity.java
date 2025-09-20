package lk.javainstitute.raula;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class FingerPrintAuthActivity extends AppCompatActivity {


    private static final String FINGERPRINT_KEY = "fingerprint_enabled"; // Key for fingerprint state
    private static final String PREFS_NAME = "UserPrefs"; // SharedPreferences name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fingerprint_auth);



        // Automatically show fingerprint prompt if enabled
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFingerprintEnabled = sharedPreferences.getBoolean(FINGERPRINT_KEY, false);

        if (isFingerprintEnabled) {
            showFingerprintPrompt();
        }
    }



    // Show fingerprint prompt
    private void showFingerprintPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(FingerPrintAuthActivity.this, "Authentication successful!", Toast.LENGTH_SHORT).show();
                        goToHomePage();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(FingerPrintAuthActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            goToPINActivity(); // Redirect to PIN entry screen
                        } else {
                            Toast.makeText(FingerPrintAuthActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                        }

                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Login")
                .setSubtitle("Authenticate using your fingerprint")
                .setNegativeButtonText("Use PIN")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    // Navigate to Home Page
    private void goToHomePage() {
        Intent intent = new Intent(FingerPrintAuthActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    // Update PIN display based on visibility state

    private void goToPINActivity() {
        Intent intent = new Intent(FingerPrintAuthActivity.this, PinCodeAuthActivity.class);
        startActivity(intent);
        finish(); // Optional: Finish the current activity
    }

}
