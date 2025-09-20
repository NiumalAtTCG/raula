package lk.javainstitute.raula;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowInsets;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.Executor;

public class SettingsActivity extends AppCompatActivity {
    private static final String FINGERPRINT_KEY = "fingerprint_enabled"; // Key to store fingerprint state
    private static final String PinCode_KEY = "pinCode_enabled"; // Key to store fingerprint state
    private static final String PREFS_NAME = "UserPrefs"; // SharedPreferences name
    private Switch fingerprintSwitch;
    private Switch pincodeSwitch;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        //Initializing Ui Element
        fingerprintSwitch = findViewById(R.id.fingerprint_auth_switch);
        pincodeSwitch = findViewById(R.id.Pin_Code_auth_switch);

        //load fingerprint state from sharedPreferance
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFingerprintEnabled = sharedPreferences.getBoolean(FINGERPRINT_KEY, false);
        fingerprintSwitch.setChecked(isFingerprintEnabled);

        // Load PIN code state from SharedPreferences
        boolean isPinCodeEnabled = sharedPreferences.getBoolean(PinCode_KEY, false);
        pincodeSwitch.setChecked(isPinCodeEnabled); // Set the PIN code switch based on stored state

        //handel fingerprint switch
        fingerprintSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Enable fingerprint authentication
               if (isBiometricAvailable()) {
                   enableFingerprintAuthentication();
               }else{
               Toast.makeText(this, "Fingerprint authentication is not available on this device.", Toast.LENGTH_SHORT).show();
               }
            } else {
                // Disable fingerprint authentication
                disableFingerprintAuthentication();
            }
        });

        // Handle pincode switch
        pincodeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Enable PIN code authentication
                Toast.makeText(this, "Pincode authentication enabled.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsActivity.this, PinEnterReenterActivity.class);
                startActivity(intent);
            } else {
                // Disable PIN code authentication
                Toast.makeText(this, "Pincode authentication disabled.", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean(PinCode_KEY, false);
                fingerprintSwitch.setChecked(false);
                editor.apply();
            }
        });
        // ✅ Set Toolbar Correctly
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);  // 🔹 First, set toolbar as action bar

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        // ✅ Adjust Padding for Status Bar (for Notch & Edge Screens)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                view.setPadding(view.getPaddingLeft(), systemBars.top, view.getPaddingRight(), view.getPaddingBottom());
                return insets;
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close activity and return to HomeActivity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(this);
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }
    private void enableFingerprintAuthentication() {



        if (fingerprintSwitch.isEnabled()) {
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putBoolean(FINGERPRINT_KEY, true);
            editor.apply();
//            Toast.makeText(this, "Fingerprint authentication enabled.", Toast.LENGTH_SHORT).show();
            showFingerprintPrompt();
        }else{
            Toast.makeText(this, "Please register first", Toast.LENGTH_SHORT).show();
            disableFingerprintAuthentication();

        }
    }
    private void disableFingerprintAuthentication() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(FINGERPRINT_KEY, false);
        editor.apply();
        Toast.makeText(this, "Fingerprint authentication disabled.", Toast.LENGTH_SHORT).show();
    }
    private void showFingerprintPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(SettingsActivity.this, "Authentication successful!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(SettingsActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                        fingerprintSwitch.setChecked(false);  // ❌ Turn switch off
                        disableFingerprintAuthentication();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(SettingsActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                        fingerprintSwitch.setChecked(false);  // ❌ Turn switch off
                        disableFingerprintAuthentication();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Login")
                .setSubtitle("Authenticate using your fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }


}
