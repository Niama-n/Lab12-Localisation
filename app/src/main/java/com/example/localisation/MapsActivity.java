package com.example.localisation;

import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders a Google Map populated with every GPS coordinate
 * previously stored on the backend.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap nma_map;
    private ServerConnector nma_server;

    private final String nma_fetchEndpoint =
            "http://192.168.43.228/localisation/showPositions.php";

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_maps);

        nma_server = new ServerConnector(this);

        SupportMapFragment nma_fragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.nma_mapFragment);
        if (nma_fragment != null) {
            nma_fragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap readyMap) {
        nma_map = readyMap;
        downloadAndPlotMarkers();
    }

    /**
     * Uses the ServerConnector to download stored positions,
     * parses each entry, and places markers on the map.
     * Also adjusts the camera to fit all visible markers.
     */
    private void downloadAndPlotMarkers() {
        nma_server.fetchJson(nma_fetchEndpoint, new ServerConnector.JsonResponseHandler() {
            @Override
            public void onSuccess(JSONObject data) {
                List<LatLng> nma_points = extractCoordinates(data);
                placeMarkersOnMap(nma_points);
                adjustCameraBounds(nma_points);
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(MapsActivity.this,
                        "Unable to load positions from server",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Parses the JSON response into a list of LatLng objects */
    private List<LatLng> extractCoordinates(JSONObject data) {
        List<LatLng> nma_result = new ArrayList<>();
        try {
            JSONArray nma_array = data.getJSONArray("positions");
            for (int i = 0; i < nma_array.length(); i++) {
                JSONObject nma_obj = nma_array.getJSONObject(i);
                double nma_lat = nma_obj.getDouble("latitude");
                double nma_lng = nma_obj.getDouble("longitude");
                nma_result.add(new LatLng(nma_lat, nma_lng));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return nma_result;
    }

    /** Drops a marker for each coordinate on the map */
    private void placeMarkersOnMap(List<LatLng> points) {
        for (int i = 0; i < points.size(); i++) {
            nma_map.addMarker(new MarkerOptions()
                    .position(points.get(i))
                    .title("Position #" + (i + 1)));
        }
    }

    /** Moves the camera so all markers are visible at once */
    private void adjustCameraBounds(List<LatLng> points) {
        if (points.isEmpty()) return;

        LatLngBounds.Builder nma_boundsBuilder = new LatLngBounds.Builder();
        for (LatLng pt : points) {
            nma_boundsBuilder.include(pt);
        }
        nma_map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(nma_boundsBuilder.build(), 100));
    }
}