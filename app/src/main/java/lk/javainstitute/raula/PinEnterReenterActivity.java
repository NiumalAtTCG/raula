package lk.javainstitute.raula;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PinEnterReenterActivity extends AppCompatActivity {
    private TextView pinEntry; // To display the entered PIN

    private StringBuilder pinCode = new StringBuilder(); // Stores the entered PIN
    private boolean isPinVisible = false; // Controls visibility of the PIN
    private boolean isReEnterMode = false; // Tracks whether user is re-entering the PIN
    private String firstPin = ""; // Stores the first entered PIN for validation
    private static final String PIN_KEY = "user_pin"; // Key to store PIN in SharedPreferences

    private static final String PinCode_KEY = "pinCode_enabled"; // Key to store fingerprint state
    private static final String PREFS_NAME = "UserPrefs"; // SharedPreferences name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pin_enter_reenter);

        // Handle window insets for EdgeToEdge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        pinEntry = findViewById(R.id.pin_entry1);

        Button registerButton = findViewById(R.id.add_profile_button);



        // Set up dial pad
        setupDialPad();

        // Set up view/hide button
        setupViewHideButton();

        // Handle register button logic
        registerButton.setOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            if (pinCode.length() == 4) {
                handleRegisterButtonClick();
            } else {
                Toast.makeText(this, "Please enter a 4-digit PIN!", Toast.LENGTH_SHORT).show();
            }
        });


    }

    // Setup the dial pad buttons and attach listeners
    private void setupDialPad() {
        int[] buttonIds = {
                R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3,
                R.id.button_4, R.id.button_5, R.id.button_6,
                R.id.button_7, R.id.button_8, R.id.button_9
        };

        for (int id : buttonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(view -> {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                if (pinCode.length() < 4) { // Limit PIN to 4 characters
                    pinCode.append(button.getText().toString());
                    updatePinDisplay();
                }
            });
        }

        // Delete button logic
        Button deleteButton = findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            if (pinCode.length() > 0) {
                pinCode.deleteCharAt(pinCode.length() - 1);
                updatePinDisplay();
            }
        });
    }

    // Setup the view/hide toggle button
    private void setupViewHideButton() {
        ImageView viewHideButton = findViewById(R.id.view_hide_button);

        viewHideButton.setOnClickListener(view -> {
            isPinVisible = !isPinVisible;
            updatePinDisplay();
            viewHideButton.setImageResource(isPinVisible ? R.drawable.eye_open : R.drawable.eye_closed);
        });
    }


// Handle PIN registration logic
    private void handleRegisterButtonClick() {
        if (!isReEnterMode) {
            firstPin = pinCode.toString(); // Save first PIN
            pinCode.setLength(0);
            updatePinDisplay();
            isReEnterMode = true;
            Toast.makeText(this, "Please re-enter your PIN", Toast.LENGTH_SHORT).show();
        } else {
            if (pinCode.toString().equals(firstPin)) {
                // Save PIN to SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(PIN_KEY, firstPin);
                editor.putBoolean(PinCode_KEY, true); // Set PIN authentication as enabled
                editor.apply();
                goToPINActivity();
            } else {
                // Reset PIN entry
                pinCode.setLength(0);
                updatePinDisplay();
                isReEnterMode = false;
                firstPin = "";
                Toast.makeText(this, "PINs do not match. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }




    // Navigate to Home Page

    private void goToPINActivity() {
        Intent intent = new Intent(PinEnterReenterActivity.this, PinCodeAuthActivity.class);
        startActivity(intent);
        finish();
    }
    // Update PIN display based on visibility state
    private void updatePinDisplay() {
        if (isPinVisible) {
            pinEntry.setText(pinCode.toString());
        } else {
            pinEntry.setText(new String(new char[pinCode.length()]).replace("\0", "*"));
        }
    }


}