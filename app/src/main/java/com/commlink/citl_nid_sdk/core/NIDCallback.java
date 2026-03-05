package com.commlink.citl_nid_sdk.core;

import com.commlink.citl_nid_sdk.model.NIDError;
import com.commlink.citl_nid_sdk.model.NIDInfo;


public interface NIDCallback {
    void onSuccess(NIDInfo nidInfo);
    void onError(NIDError error);
}

