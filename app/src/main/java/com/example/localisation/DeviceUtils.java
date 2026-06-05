package com.example.localisation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;

/**
 * Utility class responsible for resolving a stable hardware identifier
 * for the current device. This is used to tag each GPS record
 * so the server can distinguish between multiple phones.
 */
public class DeviceUtils {

    private static final String FALLBACK_TAG = "UNIDENTIFIED";

    /**
     * Attempts to retrieve the ANDROID_ID first (stable on modern devices).
     * If unavailable, tries the legacy IMEI approach.
     * Returns a fallback constant if neither method succeeds.
     */
    public static String resolveUniqueTag(Context ctx) {
        String nma_androidTag = attemptAndroidId(ctx);
        if (nma_androidTag != null) return nma_androidTag;

        String nma_legacyTag = attemptLegacyImei(ctx);
        if (nma_legacyTag != null) return nma_legacyTag;

        return FALLBACK_TAG;
    }

    private static String attemptAndroidId(Context ctx) {
        String nma_rawId = Settings.Secure.getString(
                ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (nma_rawId != null && !nma_rawId.trim().isEmpty()) {
            return nma_rawId;
        }
        return null;
    }

    @SuppressLint("HardwareIds")
    private static String attemptLegacyImei(Context ctx) {
        try {
            boolean nma_hasPermission = ActivityCompat.checkSelfPermission(
                    ctx, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            if (!nma_hasPermission) return null;

            TelephonyManager nma_tel = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            if (nma_tel == null) return null;

            String nma_imeiValue = nma_tel.getDeviceId();
            if (nma_imeiValue != null && !nma_imeiValue.trim().isEmpty()) {
                return nma_imeiValue;
            }
        } catch (SecurityException ignored) {
        }
        return null;
    }
}
