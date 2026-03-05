package com.commlink.citl_nid_sdk;

import android.app.Activity;
import android.content.Intent;

import com.commlink.citl_nid_sdk.core.NIDCallback;
import com.commlink.citl_nid_sdk.model.NIDError;
import com.commlink.citl_nid_sdk.ui.VerificationStepActivity;
import com.commlink.citl_nid_sdk.utils.CallbackHolder;


public class NIDEnterpriseSDK {

    public static void startVerification(Activity activity, String apiKey, NIDCallback callback) {
        if (activity == null) {
            if (callback != null) {
                callback.onError(new NIDError(NIDError.Code.UNKNOWN, "Activity is null"));
            }
            return;
        }

        /*if (!LicenseManager.isLicenseValid(activity.getApplicationContext(), apiKey)) {
            if (callback != null) {
                callback.onError(new NIDError(NIDError.Code.LICENSE_INVALID, "Invalid license key"));
            }
            return;
        }*/


        CallbackHolder.getInstance().setCallback(callback);
        CallbackHolder.getInstance().setLicenseKey(apiKey);

        Intent intent = new Intent(activity, VerificationStepActivity.class);
        activity.startActivity(intent);
    }
}

