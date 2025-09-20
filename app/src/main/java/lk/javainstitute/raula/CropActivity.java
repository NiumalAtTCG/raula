
package lk.javainstitute.raula;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.canhub.cropper.CropImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CropActivity extends AppCompatActivity {
    private CropImageView cropImageView;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        progressBar = findViewById(R.id.progressBar9);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Crop & Rotate");
        }

        cropImageView = findViewById(R.id.crop_image_view);
        ImageButton rotateButton = findViewById(R.id.button_rotate);
        Button saveButton = findViewById(R.id.button_save);

        Uri imageUri = getIntent().getParcelableExtra("imageUri");
        if (imageUri != null) {
            cropImageView.setImageUriAsync(imageUri);
        }

        rotateButton.setOnClickListener(v -> cropImageView.rotateImage(90));
        saveButton.setOnClickListener(v -> saveCroppedImage());
    }

    private void saveCroppedImage() {
        Bitmap croppedBitmap = cropImageView.getCroppedImage();

        if (croppedBitmap != null) {
            File file = new File(getExternalCacheDir(), "cropped_image.jpg");
            try (FileOutputStream out = new FileOutputStream(file)) {
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                Uri croppedImageUri = Uri.fromFile(file);
                uploadImageToFirebase(croppedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving cropped image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to crop image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebase(Uri croppedImageUri) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String fileName = "profile_images/" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(croppedImageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    progressBar.setVisibility(View.GONE);
                    String imageUrl = downloadUrl.toString();
                    Log.d("ImageUpload", "Image URL: " + imageUrl);
                    Toast.makeText(CropActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("imageUrl", imageUrl);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }))
                .addOnFailureListener(e -> {
                    Log.e("ImageUpload", "Upload failed", e);
                    Toast.makeText(CropActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
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