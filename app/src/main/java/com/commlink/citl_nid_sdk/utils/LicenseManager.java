package com.commlink.citl_nid_sdk.utils;


import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import java.security.MessageDigest;
import java.util.Locale;

public class LicenseManager {

    public static boolean isLicenseValid(Context context, String licenseKey) {
        if (TextUtils.isEmpty(licenseKey)) return false;

        if ("test_license_key".equals(licenseKey)) {
            return true;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(licenseKey.getBytes());
            String encoded = Base64.encodeToString(hash, Base64.NO_WRAP);
            return encoded.toLowerCase(Locale.US).startsWith("ab");
        } catch (Exception e) {
            return false;
        }
    }
}

