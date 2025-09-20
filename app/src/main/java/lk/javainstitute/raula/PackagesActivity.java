package lk.javainstitute.raula;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lk.javainstitute.raula.adapters.PackageAdapter;
import lk.javainstitute.raula.model.PackageModel;
import lk.javainstitute.raula.model.SQLiteHelper;

public class PackagesActivity extends AppCompatActivity implements PackageAdapter.OnPackageSelectedListener {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private PackageAdapter adapter;
    private List<PackageModel> basicPackageList = new ArrayList<>();
    private List<PackageModel> premiumPackageList = new ArrayList<>();
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_packages);

        recyclerView = findViewById(R.id.recycler_packages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progressBar5);
        db = FirebaseFirestore.getInstance();
        adapter = new PackageAdapter(basicPackageList, premiumPackageList, this);
        recyclerView.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Salon Packages");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                view.setPadding(view.getPaddingLeft(), systemBars.top, view.getPaddingRight(), view.getPaddingBottom());
                return insets;
            });
        }

        loadPackages();
    }

    private void loadPackages() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("packages").get().addOnCompleteListener(task -> {

            if (task.isSuccessful() && task.getResult() != null) {
                basicPackageList.clear();
                premiumPackageList.clear();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    progressBar.setVisibility(View.GONE);
                    String category = document.getId();

                    Object servicesObj = document.get("services");
                    if (!(servicesObj instanceof List)) {
                        Log.e("PackagesActivity", "Invalid services format in " + category);
                        continue;
                    }

                    List<Map<String, Object>> services = (List<Map<String, Object>>) servicesObj;
                    for (int i = 0; i < services.size(); i++) {
                        Map<String, Object> service = services.get(i);
                        if (service == null) continue;

                        // Assign index as ID
                        String id = String.valueOf(i);
                        String name = (String) service.getOrDefault("name", "Unknown");
                        String description = (String) service.getOrDefault("description", "No description");

                        double price = 0;
                        Object priceObj = service.get("price");
                        if (priceObj instanceof Number) {
                            price = ((Number) priceObj).doubleValue();
                        }

                        PackageModel packageModel = new PackageModel(id, name, description, price, category);
                        if (category.toLowerCase().contains("basic")) {
                            basicPackageList.add(packageModel);
                        } else if (category.toLowerCase().contains("premium")) {
                            premiumPackageList.add(packageModel);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.e("PackagesActivity", "Error loading packages", task.getException());
                Toast.makeText(this, "Failed to load packages. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.zoom_out);
    }

    @Override
    public void onPackageSelected(PackageModel packageModel, boolean isSelected) {
        if (packageModel != null) {
            String message = isSelected
                    ? "Selected: " + packageModel.getName() + "\nID: " + packageModel.getId() +
                    "\nDescription: " + packageModel.getDescription() + "\nPrice: " + packageModel.getPrice()
                    : "Deselected: " + packageModel.getName();

//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            if (isSelected) {
                SQLiteHelper dbHelper = new SQLiteHelper(this);
                boolean inserted = dbHelper.insertPackage(
                        packageModel.getId(),
                        packageModel.getName(),
                        packageModel.getDescription(),
                        packageModel.getPrice()
                );

                if (inserted) {
                    Toast.makeText(this, "Package added to Cart", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Package Already Added", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
