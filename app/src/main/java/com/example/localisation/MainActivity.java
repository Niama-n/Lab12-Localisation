package com.example.localisation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Locale;

/**
 * Entry point of the application.
 * Shows the user's live GPS coordinates and offers
 * navigation to the map overview screen.
 */
public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int NMA_LOCATION_PERMISSION_CODE = 200;
    private static final long NMA_MIN_INTERVAL_MS = 1_000;
    private static final float NMA_MIN_DISTANCE_M = 0f;

    private TextView nma_latitudeLabel;
    private TextView nma_longitudeLabel;
    private TextView nma_statusLabel;
    private LocationManager nma_gpsManager;
    private ServerConnector nma_server;

    // Remote endpoint that records incoming coordinates
    private final String nma_saveEndpoint =
            "http://192.168.43.228/localisation/createPosition.php";

    // ─────────────── Lifecycle ───────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        nma_server = new ServerConnector(this);
        nma_gpsManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        verifyLocationAccess();
    }

    private void initializeViews() {
        nma_latitudeLabel = findViewById(R.id.nma_latitudeText);
        nma_longitudeLabel = findViewById(R.id.nma_longitudeText);
        nma_statusLabel = findViewById(R.id.nma_statusText);

        Button nma_openMapBtn = findViewById(R.id.nma_showMapButton);
        nma_openMapBtn.setOnClickListener(v -> navigateToMap());
    }

    private void navigateToMap() {
        startActivity(new Intent(this, MapsActivity.class));
    }

    // ─────────────── Permission handling ───────────────

    private void verifyLocationAccess() {
        boolean nma_granted = hasFineLocationPermission() || hasCoarseLocationPermission();

        if (nma_granted) {
            beginTracking();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    NMA_LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code,
                                           @NonNull String[] perms,
                                           @NonNull int[] outcomes) {
        super.onRequestPermissionsResult(code, perms, outcomes);
        if (code != NMA_LOCATION_PERMISSION_CODE) return;

        if (hasFineLocationPermission() || hasCoarseLocationPermission()) {
            beginTracking();
        } else {
            Toast.makeText(this,
                    "Location access is required for this app",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ─────────────── GPS tracking ───────────────

    @SuppressLint("MissingPermission")
    private void beginTracking() {
        if (nma_gpsManager == null) return;

        nma_gpsManager.removeUpdates(this);

        boolean nma_isTracking = false;
        if (hasFineLocationPermission()
                && nma_gpsManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            nma_gpsManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    NMA_MIN_INTERVAL_MS,
                    NMA_MIN_DISTANCE_M,
                    this
            );
            nma_isTracking = true;
        }

        if ((hasFineLocationPermission() || hasCoarseLocationPermission())
                && nma_gpsManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            nma_gpsManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    NMA_MIN_INTERVAL_MS,
                    NMA_MIN_DISTANCE_M,
                    this
            );
            nma_isTracking = true;
        }

        Location nma_lastKnown = findLastKnownLocation();
        if (nma_lastKnown != null) {
            onLocationChanged(nma_lastKnown);
        } else if (nma_isTracking) {
            nma_statusLabel.setText("Waiting for location signal...");
        } else {
            nma_statusLabel.setText("Enable location services to show coordinates");
            Toast.makeText(this,
                    "Please enable GPS or network location",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ─────────────── LocationListener callbacks ───────────────

    @Override
    public void onLocationChanged(@NonNull Location loc) {
        double nma_lat = loc.getLatitude();
        double nma_lng = loc.getLongitude();

        refreshCoordinateDisplay(nma_lat, nma_lng);
        nma_statusLabel.setText(String.format(
                Locale.getDefault(),
                "Location updated via %s",
                loc.getProvider()));
        showLocationToast(nma_lat, nma_lng, loc.getAltitude(), loc.getAccuracy());

        String nma_tag = DeviceUtils.resolveUniqueTag(this);
        nma_server.pushCoordinates(nma_saveEndpoint, nma_lat, nma_lng, nma_tag);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String[] nma_labels = {"OUT_OF_SERVICE", "TEMPORARILY_UNAVAILABLE", "AVAILABLE"};
        String nma_label = (status >= 0 && status < nma_labels.length)
                ? nma_labels[status] : "UNKNOWN";

        String nma_text = String.format(
                getString(R.string.provider_new_status), provider, nma_label);
        Toast.makeText(this, nma_text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(this,
                String.format(getString(R.string.provider_enabled), provider),
                Toast.LENGTH_SHORT).show();
        beginTracking();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this,
                String.format(getString(R.string.provider_disabled), provider),
                Toast.LENGTH_SHORT).show();
        beginTracking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (nma_gpsManager != null) {
            nma_gpsManager.removeUpdates(this);
        }
    }

    // ─────────────── UI helpers ───────────────

    private void refreshCoordinateDisplay(double lat, double lng) {
        nma_latitudeLabel.setText(
                String.format(Locale.getDefault(), "Latitude: %f", lat));
        nma_longitudeLabel.setText(
                String.format(Locale.getDefault(), "Longitude: %f", lng));
    }

    private void showLocationToast(double lat, double lng,
                                   double alt, float acc) {
        String nma_message = String.format(getString(R.string.new_location),
                String.valueOf(lat), String.valueOf(lng),
                String.valueOf(alt), String.valueOf(acc));
        Toast.makeText(this, nma_message, Toast.LENGTH_LONG).show();
    }

    private boolean hasFineLocationPermission() {
        return ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCoarseLocationPermission() {
        return ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private Location findLastKnownLocation() {
        Location nma_bestLocation = null;

        if (hasFineLocationPermission()) {
            nma_bestLocation = newerLocation(
                    nma_bestLocation,
                    nma_gpsManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        }

        if (hasFineLocationPermission() || hasCoarseLocationPermission()) {
            nma_bestLocation = newerLocation(
                    nma_bestLocation,
                    nma_gpsManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
        }

        return nma_bestLocation;
    }

    private Location newerLocation(Location current, Location candidate) {
        if (candidate == null) return current;
        if (current == null) return candidate;
        return candidate.getTime() > current.getTime() ? candidate : current;
    }
}
