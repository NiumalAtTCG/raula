package lk.javainstitute.raula;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng salonLocation = new LatLng(7.038, 79.953); // Salon Raula Location
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final String API_KEY = "AIzaSyDQgaGaougLJOhLu_MBY8nfOh3n-VHMgzs"; // Replace with your API Key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add marker for Salon Raula
        mMap.addMarker(new MarkerOptions().position(salonLocation).title("Salon Raula"));

        // Move camera to Salon Raula location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(salonLocation, 14));

        // Check for location permission
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // Add marker for user location
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

                // Draw route from user location to Salon Raula
                drawRoute(userLocation, salonLocation);
            }
        });
    }

    private void drawRoute(LatLng origin, LatLng destination) {
        new Thread(() -> {
            try {
                // Construct API URL for directions
                String urlStr = "https://maps.googleapis.com/maps/api/directions/json?origin="
                        + origin.latitude + "," + origin.longitude
                        + "&destination=" + destination.latitude + "," + destination.longitude
                        + "&mode=driving"
                        + "&key=" + API_KEY;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                // Parse JSON response
                List<LatLng> routePoints = parseDirections(response.toString());

                // Draw polyline on the map (on UI thread)
                runOnUiThread(() -> drawPolyline(routePoints));

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MapsActivity", "Error fetching directions: " + e.getMessage());
            }
        }).start();
    }

    private List<LatLng> parseDirections(String jsonResponse) {
        List<LatLng> path = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray routes = jsonObject.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");

                for (int i = 0; i < steps.length(); i++) {
                    JSONObject step = steps.getJSONObject(i);
                    JSONObject start = step.getJSONObject("start_location");
                    JSONObject end = step.getJSONObject("end_location");

                    path.add(new LatLng(start.getDouble("lat"), start.getDouble("lng")));
                    path.add(new LatLng(end.getDouble("lat"), end.getDouble("lng")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    private void drawPolyline(List<LatLng> points) {
        if (points.isEmpty()) return;

        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(10)
                .color(Color.BLUE)
                .geodesic(true));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            }
        }
    }
}