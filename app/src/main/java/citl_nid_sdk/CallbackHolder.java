package citl_nid_sdk;

class CallbackHolder {

    private static CallbackHolder instance;
    private NIDCallback callback;
    private String licenseKey;

    private CallbackHolder() {}

    public static synchronized CallbackHolder getInstance() {
        if (instance == null) {
            instance = new CallbackHolder();
        }
        return instance;
    }

    public void setCallback(NIDCallback callback) {
        this.callback = callback;
    }

    public NIDCallback getCallback() {
        return callback;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void clear() {
        callback = null;
        licenseKey = null;
    }
}

