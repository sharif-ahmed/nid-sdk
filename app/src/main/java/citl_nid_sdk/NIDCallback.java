package citl_nid_sdk;

public interface NIDCallback {
    void onSuccess(NIDInfo nidInfo);
    void onError(NIDError error);
}

