package lk.javainstitute.raula;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;




import lk.javainstitute.raula.model.Feedback;

public class PaymentHistoryActivity extends AppCompatActivity {
    private LinearLayout paymetHistiryContainer;
    private FirebaseFirestore db;
    private String userId;
    private String appointmentId;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment History");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                view.setPadding(view.getPaddingLeft(), systemBars.top, view.getPaddingRight(), view.getPaddingBottom());
                return insets;
            });
        }

        db = FirebaseFirestore.getInstance();
        paymetHistiryContainer = findViewById(R.id.paymetHistiryContainer);
        progressBar = findViewById(R.id.progressBar4);
        // Get userId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");

        // Load Appointments
        if (!userId.isEmpty()) {
            loadPaymentHistory();
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPaymentHistory() {
        progressBar.setVisibility(View.VISIBLE);
        paymetHistiryContainer.removeAllViews();
        db.collection("appointments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("payment_status", "Paid")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (querySnapshot.isEmpty()) {
                        // Show message if no appointments are found
                        showNoPatmentHystoryMessage();
                        return;
                    }
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        appointmentId = doc.getId();
                        Object timestampObj = doc.get("appointment_date");
                        Object timestampObj2 = doc.get("paid_date");
                        if (timestampObj instanceof Timestamp && timestampObj2 instanceof Timestamp) {
                            Timestamp timestamp = (Timestamp) timestampObj;
                            Timestamp timestamp2 = (Timestamp) timestampObj2;

                            Date appointmentDate = timestamp.toDate();
                            Date paidDate = timestamp2.toDate();

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            String formattedDate = sdf.format(appointmentDate);
                            String formattedDate2 = sdf.format(paidDate);

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

                            fetchPackageNames(packageIdsList, packageNames -> {
                                // Format details for display
                                addAppointmentToView(appointmentId, formattedDate, time, status, Total_Price, packageNames, formattedDate2);
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("paymentHistory", "Failed to load Firestore data: " + e.getMessage());
                    Toast.makeText(this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchPackageNames(List<String> packageIdsList, ViewAppointmentActivity.OnPackageNamesFetchedListener listener) {
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
                    Log.e("PaymentHistory", "Failed to load data: " + e.getMessage());
                    Toast.makeText(PaymentHistoryActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                });
    }

    private void showNoPatmentHystoryMessage() {
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
        paymetHistiryContainer.addView(emoticon);

        TextView noAppointmentsText = new TextView(this);
        noAppointmentsText.setText("Payment History Empty");
        noAppointmentsText.setTextSize(18);
        noAppointmentsText.setTextColor(ContextCompat.getColor(this, R.color.matiria_C_text));
        noAppointmentsText.setPadding(10, 20, 10, 20);
        noAppointmentsText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        noAppointmentsText.setLayoutParams(params);
        paymetHistiryContainer.addView(noAppointmentsText);
    }

    private void addAppointmentToView(String appointmentId, String formattedDate, String time, String status, String Total_Price, String packageNames, String formattedDate2) {
        View paymentHistoryView = LayoutInflater.from(this).inflate(R.layout.paymenthistory_item, paymetHistiryContainer, false);

        TextView resitNo = paymentHistoryView.findViewById(R.id.resitno);
        TextView appointmentDate = paymentHistoryView.findViewById(R.id.tvApoinmentDate);
        TextView tvTime = paymentHistoryView.findViewById(R.id.tvTimeSlot);
        TextView tvStatus = paymentHistoryView.findViewById(R.id.tvStatus);
        TextView tot = paymentHistoryView.findViewById(R.id.tvtot_pprice);
        TextView paidate = paymentHistoryView.findViewById(R.id.paid_date);
        TextView packages = paymentHistoryView.findViewById(R.id.tvpackages);

        SpannableString spannable = new SpannableString("Recipt No: " + appointmentId);
        spannable.setSpan(new RelativeSizeSpan(0.6f), 11, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        resitNo.setText(spannable);

        appointmentDate.setText("Appointment Date: " + formattedDate);
        tvTime.setText("Time Slot: " + time);
        tvStatus.setText(status);
        tot.setText("Paid Amount: " + Total_Price);
        paidate.setText("Paid on: " + formattedDate2);
        packages.setText("packages: " + packageNames);
        paymetHistiryContainer.addView(paymentHistoryView);

        Button feedbackButton = paymentHistoryView.findViewById(R.id.button2);

        feedbackButton.setOnClickListener(v -> {
            showFeedbackDialog(appointmentId);
        });
    }

    private void showFeedbackDialog(String appointmentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_feedback, null);
        builder.setView(dialogView);

        EditText feedbackMessage = dialogView.findViewById(R.id.feedbackMessage);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        Button submitFeedbackButton = dialogView.findViewById(R.id.submitFeedbackButton);

        AlertDialog dialog = builder.create();
        dialog.show();

        submitFeedbackButton.setOnClickListener(v -> {
            String message = feedbackMessage.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            if (rating == 0) {
                Toast.makeText(this, "Please rate your experience", Toast.LENGTH_SHORT).show();
                return;
            }

            // Upload feedback to Firestore
            uploadFeedback(appointmentId, message, rating);
            dialog.dismiss();
        });
    }

    private void uploadFeedback(String appointmentId, String message, float rating) {
        // Create a Timestamp object for the current time
        Timestamp timestamp = Timestamp.now(); // Firebase method to get the current timestamp

        // Create the Feedback object
        Feedback feedback = new Feedback(userId, appointmentId, message, rating, timestamp);

        // Upload feedback to Firestore
        db.collection("feedback")
                .add(feedback)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                    Log.e("Feedback", "Error submitting feedback", e);
                });
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.zoom_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(0, R.anim.zoom_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}