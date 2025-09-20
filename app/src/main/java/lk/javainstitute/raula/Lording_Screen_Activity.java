package lk.javainstitute.raula;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Lording_Screen_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lording_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView logoImageView = findViewById(R.id.loadingimageView1);
        ImageView textImageView = findViewById(R.id.loadingimagetextView);

        Animation springAnimation = AnimationUtils.loadAnimation(this, R.anim.spring_bounce);
        Animation flingAnimation = AnimationUtils.loadAnimation(this, R.anim.fling_slide);

        logoImageView.startAnimation(springAnimation);
        textImageView.startAnimation(flingAnimation);

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE); // Or "RaulaPrefs" - be consistent
            boolean isUserRegistered = prefs.getBoolean("isRegistered", false);
            boolean isVerified = prefs.getBoolean("isVerified", false);
            boolean fingerprintEnabled = prefs.getBoolean("fingerprint_enabled", false); // Use camelCase for consistency
            boolean pinCodeEnabled = prefs.getBoolean("pinCode_enabled", false); // Use camelCase for consistency

            Intent intent;
            if (isUserRegistered && isVerified) { // No need to check fingerprintEnabled if isUserRegistered is false
                if (fingerprintEnabled) {
                    intent = new Intent(this, FingerPrintAuthActivity.class);
                } else if (pinCodeEnabled) {
                    intent = new Intent(this, PinCodeAuthActivity.class);
                }else{
                    intent = new Intent(this, HomeActivity.class);
                }
            } else {
                intent = new Intent(this, Start_Screen_Activity.class);
            }

            startActivity(intent);
            finish();
        }, 1500);
    }
}