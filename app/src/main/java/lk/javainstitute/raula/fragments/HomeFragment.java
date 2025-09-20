package lk.javainstitute.raula.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;

import lk.javainstitute.raula.PackagesActivity;
import lk.javainstitute.raula.PaymentHistoryActivity;
import lk.javainstitute.raula.R;
import lk.javainstitute.raula.ViewAppointmentActivity;
import lk.javainstitute.raula.adapters.ImageSliderAdapter;

public class HomeFragment extends Fragment {
    private ViewPager2 viewPager2;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private List<Integer> images;
    private BottomNavigationView bottomNavigationView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager2 = view.findViewById(R.id.imageSlider);
        CardView packageCard = view.findViewById(R.id.package_card);
        CardView addBooking = view.findViewById(R.id.add_booking);
        CardView appointmentViewCard = view.findViewById(R.id.view_appointment_card);
        CardView billPaymentCard = view.findViewById(R.id.billHistory_card);

        // Get Bottom Navigation from Activity
        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);

        // Handle Bottom Navigation Clicks


        addBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                // Make sure to select the Appointment tab only once
                bottomNavigationView.setSelectedItemId(R.id.nav_appointment);

                // Now open the AppointmentFragment
                openAppointmentFragment();
            }
        });


        billPaymentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                Intent intent = new Intent(requireContext(), PaymentHistoryActivity.class);
                ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                startActivity(intent, options.toBundle());
            }
        });

        packageCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                Intent intent = new Intent(requireContext(), PackagesActivity.class);
                ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                startActivity(intent, options.toBundle());
            }
        });

        appointmentViewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                Intent intent = new Intent(requireContext(), ViewAppointmentActivity.class);
                ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                startActivity(intent, options.toBundle());
            }
        });

        setupImageSlider(view);
        return view;
    }

    private void openAppointmentFragment() {
        Fragment appointmentFragment = new AppointmentFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, appointmentFragment)
                .addToBackStack("HomeFragment")  // Add the current transaction to back stack with a name
                .commit();


    }


    private void setupImageSlider(View view) {
        TabLayout tabLayout = view.findViewById(R.id.tabIndicator);

        // Sample images from drawable
        images = Arrays.asList(
                R.drawable.banner2,
                R.drawable.banner11,
                R.drawable.banner3
        );

        ImageSliderAdapter adapter = new ImageSliderAdapter(requireContext(), images);
        viewPager2.setAdapter(adapter);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // Apply smooth transition effect
        viewPager2.setPageTransformer((page, position) -> {
            float alpha = 1 - Math.abs(position);
            page.setAlpha(alpha);
        });

        // Auto-slide feature (every 5 seconds)
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPager2.getCurrentItem();
                int nextItem = (currentItem + 1) % images.size();
                viewPager2.setCurrentItem(nextItem, true);
                sliderHandler.postDelayed(this, 5000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 5000);

        // Dot indicator with TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
        }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

}
