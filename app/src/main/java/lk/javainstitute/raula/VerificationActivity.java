package lk.javainstitute.raula;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String response_otp = getIntent().getStringExtra("user_OTP");

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the OTP EditText fields
        EditText[] otpFields = new EditText[6];
        otpFields[0] = findViewById(R.id.digit1);
        otpFields[1] = findViewById(R.id.digit2);
        otpFields[2] = findViewById(R.id.digit3);
        otpFields[3] = findViewById(R.id.digit4);
        otpFields[4] = findViewById(R.id.digit5);
        otpFields[5] = findViewById(R.id.digit6);

        for (int i = 0; i < otpFields.length; i++) {
            int finalI = i;

            // TextWatcher to handle input and move focus
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // No action needed here
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && finalI < otpFields.length - 1) {
                        otpFields[finalI + 1].requestFocus(); // Move to the next field
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // No action needed here
                }
            });

            // Handle backspace to move to the previous field
            otpFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpFields[finalI].getText().toString().isEmpty() && finalI > 0) {
                        otpFields[finalI - 1].requestFocus(); // Move to the previous field
                        otpFields[finalI - 1].setText(""); // Clear the previous field
                    }
                }
                return false;
            });
        }

        // Set up the button to capture the OTP and show it as a Toast
        Button button = findViewById(R.id.add_profile_button);
        button.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            // Concatenate text from all OTP fields
            StringBuilder otp = new StringBuilder();
            for (EditText otpField : otpFields) {
                otp.append(otpField.getText().toString());
            }

            // Show the entered OTP as a Toast
            String otpString = otp.toString();
            if (otpString.length() == 6) {
                // OTP is complete
//

                // Ensure response_otp is not null and compare trimmed values
                if (response_otp != null) { // Ensure OTP is not zero (default int value)
                    Log.i("response_otp", response_otp);
                    verified();
                    if (otpString.equals(response_otp.trim())) {
                        Intent intent = new Intent(VerificationActivity.this, PasswordConfermActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(VerificationActivity.this, "OTP Does not Match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VerificationActivity.this, "Received OTP is invalid", Toast.LENGTH_SHORT).show();
                }




            } else {
                // OTP is incomplete
                Toast.makeText(VerificationActivity.this, "Please enter the full OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void verified(){
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("isVerified", true).apply();

    }
}
