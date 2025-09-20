package lk.javainstitute.raula;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import lk.javainstitute.raula.fragments.AppointmentFragment;
import lk.javainstitute.raula.fragments.HomeFragment;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private static final int REQUEST_CALL_PERMISSION = 1;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        auth = FirebaseAuth.getInstance();
        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        toolbar = findViewById(R.id.toolbar);

//Mack a Call



        // Set Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title

// Inflate custom toolbar view
        View customToolbarView = getLayoutInflater().inflate(R.layout.toolbar, null);
        toolbar.addView(customToolbarView);

        ImageView location = customToolbarView.findViewById(R.id.location_icon);
        location.setOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            Intent intent = new Intent(view.getContext(), MapsActivity.class);
            view.getContext().startActivity(intent);
        });


        ImageView phoneIcon = customToolbarView.findViewById(R.id.phone_icon);
        phoneIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                if (ContextCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
                } else {
                    makePhoneCall();
                }
            }
        });

        // Adjust toolbar padding for the notch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, insets) -> {
                @SuppressLint("WrongConstant") int topInset = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    topInset = insets.getInsets(WindowInsets.Type.statusBars()).top;
                }
                toolbar.setPadding(toolbar.getPaddingLeft(), topInset, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
                return insets.consumeSystemWindowInsets();
            });
        }



        // Set HomeFragment as the default fragment when the app starts
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_home); // Highlight Home in BottomNavigationView
        }

        // Bottom Navigation Click Listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_appointment) {
                    selectedFragment = new AppointmentFragment();
                } else if (itemId == R.id.nav_more) {
                    // Open Right Navigation Drawer
                    drawerLayout.openDrawer(GravityCompat.END);
                    return true; // Prevent fragment change
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            }
        });

        // Navigation Drawer Click Listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_profile) {
                    // Handle Profile Click
                    startActivity(new Intent(HomeActivity.this,UserProfileActivity.class));
                } else if (itemId == R.id.nav_settings) {
                    // Handle Settings Click
                    startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                } else if (itemId == R.id.nav_logout) {
                    showLogoutDialog();
                }

//                drawerLayout.closeDrawer(GravityCompat.END);
                return true;
            }
        });
    }
    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission denied, cannot make the call.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to make a direct phone call
    private void makePhoneCall() {
        String phoneNumber = "0765906977";
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        }
    }
    private void logoutFromFirebase() {
        auth.signOut();

        // Clear session data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userId");  // Remove saved user ID
        editor.putBoolean("isRegistered", false);  // Set registration status to false
        editor.putBoolean("isVerified", false);  // Set verified status to false
        editor.apply();

        goToLoginScreen();
    }
    private void goToLoginScreen() {
        // Redirect the user to the login activity (or wherever you want to navigate)
        Intent intent = new Intent(this, Lording_Screen_Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Clear the stack
        startActivity(intent);
        finish();
    }
    private void showLogoutDialog() {
        // Create a new AlertDialog.Builder instance
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false) // Prevent dismissing by tapping outside
                .setPositiveButton("Yes", (dialog, id) -> {
                    logoutFromFirebase(); // Proceed with logout
                })
                .setNegativeButton("No", (dialog, id) -> {
                    dialog.dismiss(); // Dismiss the dialog if No is clicked
                })
                .show(); // Show the dialog
    }
}
