package com.commlink.citl_nid_sdk;

public interface NIDCallback {
    void onSuccess(boolean match, float score, NIDInfo nidInfo);
    void onError(NIDError error);
}

