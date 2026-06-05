package com.example.localisation;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Handles all HTTP communication with the backend server.
 * Encapsulates Volley queue management and request building.
 */
public class ServerConnector {

    private final RequestQueue nma_queue;
    private final Context nma_ctx;

    public ServerConnector(Context context) {
        this.nma_ctx = context.getApplicationContext();
        this.nma_queue = Volley.newRequestQueue(nma_ctx);
    }

    /**
     * Pushes a GPS coordinate pair to the specified endpoint.
     * Automatically attaches the current timestamp and device tag.
     */
    public void pushCoordinates(String endpoint, double lat, double lng, String deviceTag) {
        StringRequest nma_req = new StringRequest(Request.Method.POST, endpoint,
                ok -> { /* acknowledged */ },
                fail -> Toast.makeText(nma_ctx,
                        "Connection failed: " + fail.getMessage(),
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> nma_data = new HashMap<>();
                SimpleDateFormat nma_fmt = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                nma_data.put("latitude", String.valueOf(lat));
                nma_data.put("longitude", String.valueOf(lng));
                nma_data.put("date", nma_fmt.format(new Date()));
                nma_data.put("imei", deviceTag);
                return nma_data;
            }
        };
        nma_queue.add(nma_req);
    }

    /**
     * Fetches a JSON payload from the given URL.
     * Delegates parsing to the supplied callback.
     */
    public void fetchJson(String url, JsonResponseHandler handler) {
        JsonObjectRequest nma_jsonReq = new JsonObjectRequest(
                Request.Method.POST, url, null,
                response -> handler.onSuccess(response),
                error -> handler.onFailure(error)
        );
        nma_queue.add(nma_jsonReq);
    }

    /** Callback interface for JSON responses */
    public interface JsonResponseHandler {
        void onSuccess(org.json.JSONObject data);
        void onFailure(com.android.volley.VolleyError error);
    }
}
