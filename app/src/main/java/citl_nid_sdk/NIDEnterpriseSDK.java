package citl_nid_sdk;

import android.app.Activity;
import android.content.Intent;

public class NIDEnterpriseSDK {

    public static void startVerification(Activity activity, String licenseKey, NIDCallback callback) {
        if (activity == null) {
            if (callback != null) {
                callback.onError(new NIDError(NIDError.Code.UNKNOWN, "Activity is null"));
            }
            return;
        }

        if (!LicenseManager.isLicenseValid(activity.getApplicationContext(), licenseKey)) {
            if (callback != null) {
                callback.onError(new NIDError(NIDError.Code.LICENSE_INVALID, "Invalid license key"));
            }
            return;
        }

        CallbackHolder.getInstance().setCallback(callback);
        CallbackHolder.getInstance().setLicenseKey(licenseKey);

        Intent intent = new Intent(activity, VerificationActivity.class);
        activity.startActivity(intent);
    }
}

