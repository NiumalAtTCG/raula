package lk.javainstitute.raula;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import lk.javainstitute.raula.model.PaymentHelper;
import lk.payhere.androidsdk.PHConstants;

public class ViewAppointmentActivity extends AppCompatActivity {

    private LinearLayout appointmentContainer;
    private FirebaseFirestore db;
    private String userId;
    private  static  final String TAG = "PayHereDemo";
    private  TextView textView;
    private String appointmentId ;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointment);



        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("View Appointments");
        }

        // Apply Window Insets for Edge-to-Edge Design
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                view.setPadding(view.getPaddingLeft(), systemBars.top, view.getPaddingRight(), view.getPaddingBottom());
                return insets;
            });
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        appointmentContainer = findViewById(R.id.appointmentContainer);
        progressBar = findViewById(R.id.progressBar3);
        // Get userId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        // Load Appointments
        if (!userId.isEmpty()) {
            loadAppointments();
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 110) {
            if (data == null) {
                Log.e("PaymentHelper", "Payment Failed: No response received");
                Toast.makeText(this, "Payment failed: No response received!", Toast.LENGTH_SHORT).show();
                return;
            }

            String response = data.getStringExtra(PHConstants.INTENT_EXTRA_RESULT);
            Log.d("PaymentHelper", "Payment Response: " + response);

            if (resultCode == Activity.RESULT_OK) {
                Log.d("PaymentHelper", "Payment Approved: " + response);
                updatePaymentStatus(appointmentId);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("PaymentHelper", "Payment Canceled or Failed: " + response);
                Toast.makeText(this, "Payment was canceled or failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        appointmentContainer.removeAllViews(); // Clear previous views

        // Query Firestore for appointments where userId matches
        db.collection("appointments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("payment_status", "Not Paid")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (querySnapshot.isEmpty()) {
                        // Show message if no appointments are found
                        showNoAppointmentsMessage();
                        return;
                    }
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                         appointmentId =doc.getId();
                        Object timestampObj = doc.get("appointment_date");
                        if (timestampObj instanceof com.google.firebase.Timestamp) {
                            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) timestampObj;
                            Date appointmentDate = timestamp.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String formattedDate = sdf.format(appointmentDate);

                            String time = doc.getString("time_slot");
                            String status = doc.getString("payment_status");
                            List<String> packageIdsList = (List<String>) doc.get("packageIds");
                            double totalPrice = 0.0;
                            if (doc.contains("totalPrice")) {
                                if (doc.get("totalPrice") instanceof Long) {
                                    totalPrice = doc.getLong("totalPrice"); // If stored as Long
                                } else if (doc.get("totalPrice") instanceof Double) {
                                    totalPrice = doc.getDouble("totalPrice"); // If stored as Double
                                }
                            }
                            String Total_Price = String.format(Locale.getDefault(), "%.2f", totalPrice);
                            db.collection("app_users").document(userId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String firstName = userDoc.getString("fname");
                                            String lastName = userDoc.getString("lname");
                                            String mobile = userDoc.getString("mobile");
                                            String email = userDoc.getString("email");

                                            fetchPackageNames(packageIdsList, packageNames -> {
                                                // Format details for display
                                                addAppointmentToView(appointmentId,formattedDate, time, status,Total_Price,packageNames,firstName,lastName,mobile,email);


                                            });
                                        }
                                    });


                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ViewAppointmentActivity", "Failed to load Firestore data: " + e.getMessage());
                    Toast.makeText(this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
                });
    }
    private void fetchPackageNames(List<String> packageIdsList, OnPackageNamesFetchedListener listener) {
        if (packageIdsList == null || packageIdsList.isEmpty()) {
            listener.onPackageNamesFetched("No packages selected");
            return;
        }

        db.collection("packages").document("basic_salon").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> services = (List<Map<String, Object>>) documentSnapshot.get("services");

                        if (services != null) {
                            List<String> packageDetailsList = new ArrayList<>();

                            Log.d("FetchPackages", "Retrieved services: " + services.toString());
                            Log.d("FetchPackages", "Package indices received: " + packageIdsList.toString());

                            for (String idStr : packageIdsList) {
                                try {
                                    int index = Integer.parseInt(idStr.trim());

                                    if (index >= 0 && index < services.size()) {
                                        Map<String, Object> service = services.get(index);
                                        Log.d("FetchPackages", "Fetching service at index " + index + ": " + service.toString());

                                        String packageName = service.get("name").toString();
                                        String packagePrice = service.get("price").toString();
                                        packageDetailsList.add(packageName + " (Rs. " + packagePrice + ")");
                                    } else {
                                        Log.w("FetchPackages", "Index out of bounds: " + index);
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("FetchPackages", "Invalid index format: " + idStr, e);
                                }
                            }

                            listener.onPackageNamesFetched(String.join(", ", packageDetailsList));
                        } else {
                            Log.w("FetchPackages", "No services available");
                            listener.onPackageNamesFetched("No services available");
                        }
                    } else {
                        Log.w("FetchPackages", "Package data not found");
                        listener.onPackageNamesFetched("Package data not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchPackages", "Error fetching packages: " + e.getMessage());
                    listener.onPackageNamesFetched("Error fetching packages");
                });
    }




    // Interface to handle asynchronous Firestore calls
    interface OnPackageNamesFetchedListener {
        void onPackageNamesFetched(String packageNames);
    }
    private void showNoAppointmentsMessage() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.CENTER;

        TextView emoticon = new TextView(this);
        emoticon.setText("¯\\_(ツ)_/¯");
        emoticon.setTextSize(50);
        emoticon.setPadding(10, 700, 10, 20);
        emoticon.setTextColor(ContextCompat.getColor(this, R.color.matiria_C_lignt));
        emoticon.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        emoticon.setLayoutParams(params);
        appointmentContainer.addView(emoticon);

        TextView noAppointmentsText = new TextView(this);
        noAppointmentsText.setText("There are no appointments\n to view");
        noAppointmentsText.setTextSize(18);
        noAppointmentsText.setTextColor(ContextCompat.getColor(this, R.color.matiria_C_text));
        noAppointmentsText.setPadding(10, 20, 10, 20);
        noAppointmentsText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        noAppointmentsText.setLayoutParams(params);
        appointmentContainer.addView(noAppointmentsText);
    }


    // Method to add appointment to the view
    private void addAppointmentToView(String appointmentId , String date, String time, String status,String Total_Price,String packageNames, String email,String firstName,String lastName ,String mobile ) {
        View appointmentView = LayoutInflater.from(this).inflate(R.layout.appointment_item, appointmentContainer, false);

        TextView tvDate = appointmentView.findViewById(R.id.resitno);
        TextView tvTime = appointmentView.findViewById(R.id.tvTimeSlot);
        TextView tvStatus = appointmentView.findViewById(R.id.tvStatus);
        TextView tot = appointmentView.findViewById(R.id.tvtot_pprice);
        TextView packages = appointmentView.findViewById(R.id.tvpackages);

        tvDate.setText("Date: " + date);
        tvTime.setText( time);
        tvStatus.setText(status);
        tot.setText("Total Payment: " + Total_Price);
        packages.setText("packages: " + packageNames);
        appointmentContainer.addView(appointmentView);

        Button payNowButton = appointmentView.findViewById(R.id.button2);

        payNowButton.setOnClickListener(v -> {
            // Get the total payment amount
            PaymentHelper.initiatePayment(ViewAppointmentActivity.this, Double.parseDouble(Total_Price), appointmentId, packageNames,email,firstName,lastName,mobile);
            // Show the total payment in a Toast

        });

    }
    // Method to update the payment status in Firestore
    private void updatePaymentStatus(String appointmentId) {
        // Reference to the appointment document in Firestore
        DocumentReference appointmentRef = db.collection("appointments").document(appointmentId);

        // Create a map with new values
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("payment_status", "Paid");
        updateData.put("paid_date", Timestamp.now()); // Add current date & time

        // Update Firestore document with merge to avoid overwriting other fields
        appointmentRef.set(updateData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("PaymentUpdate", "Payment status updated successfully with paid_date");

                    // Refresh appointments to show updated status
                    loadAppointments();
                    goToPaymentHistory();
                })
                .addOnFailureListener(e -> {
                    Log.e("PaymentUpdate", "Error updating payment status", e);
                    Toast.makeText(ViewAppointmentActivity.this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
                });
    }


    // Method to navigate to the Payment History Activity (or any other activity)
    private void goToPaymentHistory() {
        Intent intent = new Intent(ViewAppointmentActivity.this, PaymentHistoryActivity.class);
        startActivity(intent);
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