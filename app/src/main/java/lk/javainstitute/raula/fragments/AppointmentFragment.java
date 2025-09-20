package lk.javainstitute.raula.fragments;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lk.javainstitute.raula.PackagesActivity;
import lk.javainstitute.raula.R;
import lk.javainstitute.raula.adapters.SelectedPackagesAdapter;
import lk.javainstitute.raula.model.SQLiteHelper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class AppointmentFragment extends Fragment {

    private FloatingActionButton btnSelectDate, fabAddPackage;
    private TextView tvSelectedDate, tvTotalPrice;
    private RecyclerView recyclerView;
    private SelectedPackagesAdapter selectedPackagesAdapter;
    private SQLiteHelper sqLiteHelper;
    private Button btnBookAppointment;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private static final String PREFS_NAME = "UserPrefs";

    private static final int REQUEST_PACKAGE_SELECTION = 1001;
    private Calendar selectedCalendar = Calendar.getInstance();
    private ProgressBar progressBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        sqLiteHelper = new SQLiteHelper(getContext());
        createNotificationChannel();
        // Initialize UI elements
        btnSelectDate = view.findViewById(R.id.btn_select_date);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        fabAddPackage = view.findViewById(R.id.fab_add_package);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnBookAppointment = view.findViewById(R.id.add_appointment);
        recyclerView = view.findViewById(R.id.recycler_selected_packages);
        progressBar = view.findViewById(R.id.progressBar11);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        enableSwipeToDelete();

        tvSelectedDate.setText("(\u2060.\u2060 \u2060❛\u2060 \u2060ᴗ\u2060 \u2060❛\u2060.\u2060)");
        tvSelectedDate.setTextSize(30);

        // Select Date Handler
        btnSelectDate.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        tvSelectedDate.setText(sdf.format(selectedCalendar.getTime()));
                        tvSelectedDate.setTextSize(18);
                    },
                    selectedCalendar.get(Calendar.YEAR),
                    selectedCalendar.get(Calendar.MONTH),
                    selectedCalendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() + 86400000);
            datePicker.show();
        });

        // Add Package Button
        fabAddPackage.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            Intent intent = new Intent(getActivity(), PackagesActivity.class);
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
            startActivityForResult(intent, REQUEST_PACKAGE_SELECTION, options.toBundle());
        });

        // Book Appointment Button
        btnBookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                showConfirmationDialog();
            }
        });


        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("AppointmentFragment", "Resumed - Reloading Packages");
        loadSelectedPackages();

    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // Use requireContext()
        builder.setTitle("Confirm Booking");
        builder.setMessage("Are you sure you want to book this appointment?");

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            bookAppointment(); // Proceed with booking
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss(); // Close the dialog
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void loadSelectedPackages() {
        new Thread(() -> {
            SQLiteDatabase sqLiteDatabase = sqLiteHelper.getReadableDatabase();
            Cursor tempCursor = sqLiteDatabase.query("packages", null, null, null, null, null, "id DESC");

            double totalPrice = 0.0;
            if (tempCursor != null) {
                while (tempCursor.moveToNext()) {
                    double price = tempCursor.getDouble(tempCursor.getColumnIndexOrThrow("price"));
                    totalPrice += price;
                }

                final double finalTotalPrice = totalPrice;
                requireActivity().runOnUiThread(() -> {
                    if (selectedPackagesAdapter == null) {
                        selectedPackagesAdapter = new SelectedPackagesAdapter(getContext(), tempCursor);
                        recyclerView.setAdapter(selectedPackagesAdapter);
                    } else {
                        selectedPackagesAdapter.updateCursor(tempCursor);
                    }
                    tvTotalPrice.setText("Rs" + finalTotalPrice);
                    recyclerView.invalidate();
                });

            } else {
                Log.d("AppointmentFragment", "No packages selected.");
            }
        }).start();
    }
    private String getUserIdFromPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);  // Use PREFS_NAME
        return sharedPreferences.getString("userId", null); // Return the user ID, or null if not found
    }


    private void bookAppointment() {

        String userId = getUserIdFromPreferences();
        if (userId == null || tvSelectedDate.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Please select a date and login!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the selected date is today
        Calendar today = Calendar.getInstance();
        boolean isToday = selectedCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                selectedCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                selectedCalendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);

        if (isToday) {
            Toast.makeText(getContext(), "Cannot book appointments for today. Try another day!", Toast.LENGTH_SHORT).show();
            return; // Exit the method if the selected date is today
        }

        List<String> packageIds = new ArrayList<>();
        double totalPrice = 0.0;
        SQLiteDatabase dbRead = sqLiteHelper.getReadableDatabase();
        Cursor cursor = dbRead.query("packages", null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            packageIds.add(cursor.getString(cursor.getColumnIndexOrThrow("id")));
            totalPrice += cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
        }
        cursor.close();

        if (packageIds.isEmpty()) {
            Toast.makeText(getContext(), "No packages selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp appointmentTimestamp = new Timestamp(selectedCalendar.getTime());
        Log.d("BookAppointment", "Selected date: " + appointmentTimestamp.toDate());

        // Define the start and end of the selected date
        Calendar startOfDay = (Calendar) selectedCalendar.clone();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = (Calendar) selectedCalendar.clone();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);

        Timestamp startTimestamp = new Timestamp(startOfDay.getTime());
        Timestamp endTimestamp = new Timestamp(endOfDay.getTime());
        progressBar.setVisibility(View.VISIBLE);
        db.collection("appointments")
                .whereGreaterThanOrEqualTo("appointment_date", startTimestamp)
                .whereLessThanOrEqualTo("appointment_date", endTimestamp)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    Set<String> bookedTimeSlots = new HashSet<>();
                    Log.d("BookAppointment", "Total appointments found for date: " + querySnapshot.size());

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String bookedSlot = document.getString("time_slot");
                        if (bookedSlot != null) {
                            bookedTimeSlots.add(bookedSlot);
                            Log.d("BookAppointment", "Booked slot from Firestore: " + bookedSlot);
                        }
                    }

                    // Assign the next available time slot
                    String assignedTimeSlot = getNextAvailableTimeSlot(bookedTimeSlots);
                    Log.d("BookAppointment", "Assigned slot after checking booked slots: " + assignedTimeSlot);

                    if (assignedTimeSlot.equals("No available slots")) {
                        Toast.makeText(getContext(), "No available slots. Try another day!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Proceed with booking
                    Map<String, Object> appointmentData = new HashMap<>();
                    appointmentData.put("userId", userId);
                    appointmentData.put("packageIds", packageIds);
                    appointmentData.put("totalPrice", Double.parseDouble(tvTotalPrice.getText().toString().replace("Rs", "")));
                    appointmentData.put("appointment_date", appointmentTimestamp);
                    appointmentData.put("time_slot", assignedTimeSlot);
                    appointmentData.put("request_date", Timestamp.now());
                    appointmentData.put("appointment_Status", "Accepted");
                    appointmentData.put("payment_status", "Not Paid");

                    db.collection("appointments").add(appointmentData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("BookAppointment", "Appointment booked successfully!");
                                Toast.makeText(getContext(), "Appointment booked!", Toast.LENGTH_SHORT).show();
                                sendNotification(appointmentTimestamp, assignedTimeSlot);
                                clearSelection();

                            })
                            .addOnFailureListener(e -> {
                                Log.e("BookAppointment", "Booking failed!", e);
                                Toast.makeText(getContext(), "Booking failed!", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> Log.e("BookAppointment", "Error fetching appointments!", e));
    }


    // 📌 **Helper Method: Assign Next Available Slot**
    private String getNextAvailableTimeSlot(Set<String> bookedTimeSlots) {
        String[] timeSlots = {
                "09:00 - 10:00 AM", "10:00 - 11:00 AM", "11:00 - 12:00 PM",
                "12:00 - 01:00 PM", "01:00 - 02:00 PM", "02:00 - 03:00 PM",
                "03:00 - 04:00 PM", "04:00 - 05:00 PM", "05:00 - 06:00 PM"
        };

        Log.d("GetTimeSlot", "Checking available time slots...");
        Log.d("GetTimeSlot", "Booked slots so far: " + bookedTimeSlots);

        for (String slot : timeSlots) {
            if (!bookedTimeSlots.contains(slot)) {
                Log.d("GetTimeSlot", "Available slot found: " + slot);
                return slot;
            }
        }

        Log.d("GetTimeSlot", "All slots are booked for this day!");
        return "No available slots";
    }


    // 🎯 **Helper Method: Clear Selection After Booking**
    private void clearSelection() {
        selectedPackagesAdapter.updateCursor(null);
        tvSelectedDate.setText("Select Date");
        tvTotalPrice.setText("Rs:0.00");
    }

    private void enableSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Get the position of the item that was swiped
                int position = viewHolder.getAdapterPosition();

                // Get the cursor from the adapter
                Cursor cursor = (Cursor) selectedPackagesAdapter.getItem(position);

                // Get the package ID to delete from SQLite
                if (cursor != null) {
                    String packageId = cursor.getString(cursor.getColumnIndexOrThrow("id"));

                    // Delete the package from SQLite
                    deletePackageFromDatabase(packageId);

                    // Notify the adapter to update the list
                    loadSelectedPackages();
                }


                if (direction == ItemTouchHelper.LEFT) {
                    Log.d("Swipe", "Swiped Left");
                } else if (direction == ItemTouchHelper.RIGHT) {
                    Log.d("Swipe", "Swiped Right");
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void deletePackageFromDatabase(String packageId) {
        SQLiteDatabase dbWrite = sqLiteHelper.getWritableDatabase();
        String whereClause = "id = ?";
        String[] whereArgs = new String[] { packageId };

        int rowsDeleted = dbWrite.delete("packages", whereClause, whereArgs);

        if (rowsDeleted > 0) {
            Log.d("AppointmentFragment", "Package deleted: " + packageId);
        } else {
            Log.e("AppointmentFragment", "Failed to delete package: " + packageId);
        }
    }

    private void animateClearSelection() {
        // Fade out the selected packages RecyclerView
        ObjectAnimator fadeOutRecyclerView = ObjectAnimator.ofFloat(recyclerView, "alpha", 1f, 0f);
        fadeOutRecyclerView.setDuration(500); // 500ms for fade out
        fadeOutRecyclerView.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Clear the RecyclerView after the fade out
                selectedPackagesAdapter.updateCursor(null);
                tvTotalPrice.setText("$0.00");
                recyclerView.setVisibility(View.INVISIBLE);

                // Fade in the RecyclerView for smooth transition
                recyclerView.setVisibility(View.VISIBLE);
                ObjectAnimator fadeInRecyclerView = ObjectAnimator.ofFloat(recyclerView, "alpha", 0f, 1f);
                fadeInRecyclerView.setDuration(500); // 500ms for fade in
                fadeInRecyclerView.start();
            }
        });
        fadeOutRecyclerView.start();


        ObjectAnimator fadeOutDate = ObjectAnimator.ofFloat(tvSelectedDate, "alpha", 1f, 0f);
        fadeOutDate.setDuration(500);
        fadeOutDate.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tvSelectedDate.setText("(\u2060.\u2060 \u2060❛\u2060 \u2060ᴗ\u2060 \u2060❛\u2060.\u2060)");// Clear text field
                tvSelectedDate.setTextSize(30);

                ObjectAnimator fadeInDate = ObjectAnimator.ofFloat(tvSelectedDate, "alpha", 0f, 1f);
                fadeInDate.setDuration(500);
                fadeInDate.start();
            }
        });
        fadeOutDate.start();

        ObjectAnimator fadeOutPrice = ObjectAnimator.ofFloat(tvTotalPrice, "alpha", 1f, 0f);
        fadeOutPrice.setDuration(500);
        fadeOutPrice.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tvTotalPrice.setText("Rs:0.00");

                ObjectAnimator fadeInPrice = ObjectAnimator.ofFloat(tvTotalPrice, "alpha", 0f, 1f);
                fadeInPrice.setDuration(500);
                fadeInPrice.start();
            }
        });
        fadeOutPrice.start();
    }
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Booking Notifications";
            String description = "Notifications for booking status";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("booking_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void sendNotification(Timestamp appointmentTimestamp, String timeSlot) {
        Context context = requireContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if NotificationManager is available
        if (notificationManager == null) {
            Log.e("NotificationDebug", "NotificationManager is null. Cannot send notification.");
            return;
        }

        // Convert timestamp to a readable date format
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String appointmentDate = sdf.format(appointmentTimestamp.toDate());

        // Log the appointment date and time slot
        Log.d("NotificationDebug", "Appointment Date: " + appointmentDate);
        Log.d("NotificationDebug", "Time Slot: " + timeSlot);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "booking_channel")
                .setSmallIcon(R.drawable.logo_raula) // Replace with your notification icon
                .setContentTitle("Your booking is successful!")
                .setContentText("Appointment Date: " + appointmentDate + "\nTime Slot: " + timeSlot)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);


        Log.d("NotificationDebug", "Notification built with title: " + builder.build().extras.getString("android.title"));
        Log.d("NotificationDebug", "Notification content: " + builder.build().extras.getString("android.text"));

        // Check if the notification channel exists (for Android 8.0 and above)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel("booking_channel");
            if (channel == null) {
                Log.e("NotificationDebug", "Notification channel 'booking_channel' does not exist.");
                return;
            } else {
                Log.d("NotificationDebug", "Notification channel 'booking_channel' exists.");
            }
        }

        // Show the notification
        notificationManager.notify(1, builder.build());
        Log.d("NotificationDebug", "Notification sent successfully.");
    }
}
