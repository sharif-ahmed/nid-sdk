package com.commlink.citl_nid_sdk;

import java.lang.ref.WeakReference;

class CallbackHolder {

    private static CallbackHolder instance;
    private WeakReference<NIDCallback> callbackRef;
    private String licenseKey;

    private CallbackHolder() {}

    public static synchronized CallbackHolder getInstance() {
        if (instance == null) {
            instance = new CallbackHolder();
        }
        return instance;
    }

    public void setCallback(NIDCallback callback) {
        this.callbackRef = new WeakReference<>(callback);
    }

    public NIDCallback getCallback() {
        return callbackRef != null ? callbackRef.get() : null;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void clear() {
        if (callbackRef != null) {
            callbackRef.clear();
        }
        callbackRef = null;
        licenseKey = null;
    }
}

