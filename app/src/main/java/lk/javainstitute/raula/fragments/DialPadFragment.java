package lk.javainstitute.raula.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import lk.javainstitute.raula.R;
import lk.javainstitute.raula.FingerPrintAuthActivity;
import lk.javainstitute.raula.VerificationActivity;

public class DialPadFragment extends Fragment {

    private DialPadListener dialPadListener;
    private TextView pinEntry;
    private StringBuilder pinCode = new StringBuilder();
    private static final String PREFS_NAME = "RaulaPrefs";
    private static final String PIN_KEY = "user_pin"; // This key stores the user's PIN
    private boolean isPinVisible = false;

    // Listener interface to communicate with the hosting activity
    public interface DialPadListener {
        void onDigitEntered(String digit);
        void onDeletePressed();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DialPadListener) {
            dialPadListener = (DialPadListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement DialPadListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dial_pad, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        pinEntry = view.findViewById(R.id.pin_entry1);
        Button loginButton = view.findViewById(R.id.add_profile_button);

        // Setup dial pad buttons (0-9)
        int[] buttonIds = {
                R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3,
                R.id.button_4, R.id.button_5, R.id.button_6,
                R.id.button_7, R.id.button_8, R.id.button_9
        };

        for (int id : buttonIds) {
            Button button = view.findViewById(id);
            button.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                if (pinCode.length() < 4) {  // Limit to 4 digits
                    pinCode.append(button.getText().toString());
                    updatePinDisplay();
                }
            });
        }

        // Delete button logic
        Button deleteButton = view.findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            if (pinCode.length() > 0) {
                pinCode.deleteCharAt(pinCode.length() - 1);
                updatePinDisplay();
            }
        });

        // Handle login button logic
        loginButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            if (pinCode.length() == 4) {
                handleLoginButtonClick();  // Proceed with login when a 4-digit PIN is entered
            } else {
                Toast.makeText(getActivity(), "Please enter a 4-digit PIN!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to update PIN entry display (using "*" for each entered digit)
    private void updatePinDisplay() {
        pinEntry.setText(new String(new char[pinCode.length()]).replace("\0", "*"));
    }

    // Handle login button click logic
    private void handleLoginButtonClick() {
        // Retrieve the saved PIN from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedPin = sharedPreferences.getString(PIN_KEY, "");

        // Validate the entered PIN
        if (savedPin.isEmpty()) {
            // If there's no saved PIN, prompt for registration
            Toast.makeText(getActivity(), "Please register a PIN first.", Toast.LENGTH_SHORT).show();
            goToSignUpActivity();
        } else if (pinCode.toString().equals(savedPin)) {
            // If the PIN is correct, proceed to home page
            Toast.makeText(getActivity(), "Login successful!", Toast.LENGTH_SHORT).show();
            goToHomePage();
        } else {
            // If the PIN is incorrect, reset and show an error message
            Toast.makeText(getActivity(), "Invalid PIN. Please try again.", Toast.LENGTH_SHORT).show();
            pinCode.setLength(0);  // Clear the entered PIN
            updatePinDisplay();
        }
    }

    // Navigate to the home page after successful login
    private void goToHomePage() {
        Intent intent = new Intent(getActivity(), VerificationActivity.class);
        startActivity(intent);
        getActivity().finish();  // Close current activity
    }

    // Navigate to the sign-up page (for PIN registration)
    private void goToSignUpActivity() {
        Intent intent = new Intent(getActivity(), FingerPrintAuthActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
