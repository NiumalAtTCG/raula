package lk.javainstitute.raula;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CROP_IMAGE_REQUEST = 2;
    private ImageView profileImageView;
    private LinearLayout editDeleteLayout;
    private TextView editTextView, deleteTextView, add_picture_text;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private TextInputLayout fnameInput, lnameInput, mobileInput, emailInput, addressInput;
    private String userId;
    private ProgressBar progressBar;
    private static final String PREFS_NAME = "UserPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        userprofileActivitylayout();

        fnameInput = findViewById(R.id.textfnameInputLayout);
        lnameInput = findViewById(R.id.textlnameInputLayout);
        mobileInput = findViewById(R.id.textmobileInputLayout);
        emailInput = findViewById(R.id.textemailInputLayout);
        addressInput = findViewById(R.id.textaddressInputLayout);
        progressBar = findViewById(R.id.progressBar2);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);
        Log.d("UserProfileActivity", "User ID: " + userId);

        if (userId != null && !userId.isEmpty()) {
            loadUserData(userId);
        } else {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
        }

        Button updateProfileButton = findViewById(R.id.update_profile_button);
        updateProfileButton.setOnClickListener(view -> updateUserData());
    }

    private void updateUserData() {
        String fname = fnameInput.getEditText().getText().toString().trim();
        String lname = lnameInput.getEditText().getText().toString().trim();
        String mobile = mobileInput.getEditText().getText().toString().trim();
        String email = emailInput.getEditText().getText().toString().trim();
        String address = addressInput.getEditText().getText().toString().trim();

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("app_users").document(userId);

        Map<String, Object> updates = new HashMap<>();
        if (!fname.isEmpty()) updates.put("fname", fname);
        if (!lname.isEmpty()) updates.put("lname", lname);
        if (!mobile.isEmpty()) updates.put("mobile", mobile);
        if (!email.isEmpty()) updates.put("email", email);
        if (!address.isEmpty()) updates.put("address", address);

        if (updates.isEmpty()) {
            Toast.makeText(this, "No changes detected!", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show();
                    Log.e("UserProfileActivity", "Error updating user data", e);
                });
    }

    private void loadUserData(String userId) {
        db = FirebaseFirestore.getInstance();
        progressBar.setVisibility(View.VISIBLE);
        DocumentReference userRef = db.collection("app_users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            progressBar.setVisibility(View.GONE);
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("fname")) {
                    fnameInput.getEditText().setHint(documentSnapshot.getString("fname"));
                }
                if (documentSnapshot.contains("lname")) {
                    lnameInput.getEditText().setHint(documentSnapshot.getString("lname"));
                }
                if (documentSnapshot.contains("email")) {
                    emailInput.getEditText().setHint(documentSnapshot.getString("email"));
                }
                if (documentSnapshot.contains("mobile")) {
                    mobileInput.getEditText().setHint(documentSnapshot.getString("mobile"));
                }
                if (documentSnapshot.contains("address")) {
                    addressInput.getEditText().setHint(documentSnapshot.getString("address"));
                }
                if (documentSnapshot.contains("profileImageUrl")) {
                    String imageUrl = documentSnapshot.getString("profileImageUrl");
                    Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(profileImageView);

                    profileImageView.setPadding(0, 0, 0, 0);
                    editDeleteLayout.setVisibility(View.VISIBLE);
                    add_picture_text.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            Log.e("UserProfileActivity", "Error loading user data", e);
        });
    }

    private void userprofileActivitylayout() {
        profileImageView = findViewById(R.id.profile_image_view);
        editDeleteLayout = findViewById(R.id.edit_delete_layout);
        editTextView = findViewById(R.id.edit_text_view);
        add_picture_text = findViewById(R.id.add_picture_text);
        deleteTextView = findViewById(R.id.delete_text_view);

        editDeleteLayout.setVisibility(View.GONE);
        add_picture_text.setVisibility(View.VISIBLE);

        profileImageView.setPadding(dpToPx(25), dpToPx(25), dpToPx(25), dpToPx(25));

        profileImageView.setOnClickListener(v -> openGallery());
        editTextView.setOnClickListener(v -> openGallery());
        deleteTextView.setOnClickListener(v -> resetProfileImage());

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                view.setPadding(view.getPaddingLeft(), systemBars.top, view.getPaddingRight(), view.getPaddingBottom());
                return insets;
            });
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            startCropActivity(selectedImageUri);
        } else if (requestCode == CROP_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            String imageUrl = data.getStringExtra("imageUrl");
            if (imageUrl != null) {
                Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .into(profileImageView);

                profileImageView.setPadding(10, 10, 10, 10);
                editDeleteLayout.setVisibility(View.VISIBLE);
                add_picture_text.setVisibility(View.GONE);

                saveImageUrlToFirestore(imageUrl);
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        if (userId != null) {
            DocumentReference userRef = db.collection("app_users").document(userId);
            userRef.update("profileImageUrl", imageUrl)
                    .addOnSuccessListener(aVoid -> Log.d("UserProfileActivity", "Profile image URL updated"))
                    .addOnFailureListener(e -> Log.e("UserProfileActivity", "Error updating profile image URL", e));
        }
    }

    private void startCropActivity(Uri uri) {
        Intent intent = new Intent(this, CropActivity.class);
        intent.putExtra("imageUri", uri);
        startActivityForResult(intent, CROP_IMAGE_REQUEST);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    private void resetProfileImage() {
        selectedImageUri = null;
        Glide.with(this).clear(profileImageView);
        profileImageView.setImageResource(R.drawable.add_photo_alternate);
        editDeleteLayout.setVisibility(View.GONE);
        add_picture_text.setVisibility(View.VISIBLE);
        profileImageView.setPadding(dpToPx(25), dpToPx(25), dpToPx(25), dpToPx(25));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}