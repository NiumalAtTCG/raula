package lk.javainstitute.raula.model;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import lk.javainstitute.raula.R;
import lk.javainstitute.raula.VerificationActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ForgotPasswordBottomSheet extends BottomSheetDialogFragment {

    private EditText emailEditText;
    private Button sendResetLinkButton;

    private static final String TAG = "ForgotPasswordActivity";
    private String otp;

    public ForgotPasswordBottomSheet() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_forgot_password, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        sendResetLinkButton = view.findViewById(R.id.sendResetLinkButton);

        sendResetLinkButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getActivity(), "Enter your email", Toast.LENGTH_SHORT).show();
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getActivity(), "Enter a valid email", Toast.LENGTH_SHORT).show();
            } else {
                // Send OTP request to the server
                sendOtpToEmail(email);
            }
        });

        return view;
    }

    private void sendOtpToEmail(String email) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://5d97-112-135-71-237.ngrok-free.app/raula/send_OTP?email=" + email)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String responseText = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseText).getAsJsonObject();
                otp = jsonResponse.get("content").getAsString().trim();

                Log.i(TAG, "Generated OTP: " + otp);

                // Run on UI thread to update UI
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> navigateToVerification(otp));
                }
            } catch (Exception e) {
                Log.e(TAG, "OTP request failed", e);
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity, "Failed to send OTP", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }

    private void navigateToVerification(String otp) {
        Intent intent = new Intent(getActivity(), VerificationActivity.class);
        intent.putExtra("user_OTP", otp);
        startActivity(intent);
        dismiss(); // Close the BottomSheetDialogFragment
    }
}
